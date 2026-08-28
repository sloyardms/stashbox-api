package com.sloyardms.stashboxapi.domain.note.service;

import com.sloyardms.stashboxapi.domain.note.model.ItemNote;
import com.sloyardms.stashboxapi.domain.note.model.NoteFile;
import com.sloyardms.stashboxapi.domain.note.repository.NoteFileRepository;
import com.sloyardms.stashboxapi.infrastructure.storage.AttachmentProperties;
import com.sloyardms.stashboxapi.infrastructure.storage.PendingUpload;
import com.sloyardms.stashboxapi.infrastructure.storage.StoredFile;
import com.sloyardms.stashboxapi.infrastructure.storage.event.ItemNoteHardDeleteEvent;
import com.sloyardms.stashboxapi.infrastructure.storage.event.NoteFilesHardDeleteEvent;
import com.sloyardms.stashboxapi.infrastructure.storage.service.FileStorageService;
import com.sloyardms.stashboxapi.shared.exception.FieldErrorDetail;
import com.sloyardms.stashboxapi.shared.exception.types.FieldValidationException;
import com.sloyardms.stashboxapi.shared.utils.FileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class NoteFileService {

    private final FileStorageService fileStorageService;
    private final NoteFileRepository noteFileRepository;
    private final NoteFileCleanupService noteFileCleanupService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AttachmentProperties attachmentProperties;

    @Transactional(rollbackFor = Exception.class)
    public List<NoteFile> createNoteFiles(ItemNote note, List<MultipartFile> files){
        List<NoteFile> noteFiles = new ArrayList<>();
        List<PendingUpload> pendingUploads = new ArrayList<>();
        List<MultipartFile> safeFiles = files == null ? List.of() : files;

        validateAttachments(0, safeFiles);

        for(int i = 0; i < safeFiles.size(); i++){
            MultipartFile file = safeFiles.get(i);

            // generate soted file metadata
            UUID noteFileId = UUID.randomUUID();
            String detectedMimeType = FileValidator.detectMimeType(file);
            StoredFile storedFile = fileStorageService
                    .generateStoredFileMetadata(note.getUser().getId(), note.getItem().getId(),
                            note.getId(), noteFileId, file, detectedMimeType);

            // create note file entity
            NoteFile noteFile = new NoteFile();
            noteFile.setId(noteFileId);
            noteFile.setUser(note.getUser());
            noteFile.setNote(note);
            noteFile.setOriginalFilename(file.getOriginalFilename());
            noteFile.setStoredFilename(storedFile.getStoredFilename());
            noteFile.setFilePath(storedFile.getRelativeFilePath());
            noteFile.setMimeType(detectedMimeType);
            noteFile.setFileSize(file.getSize());
            noteFile.setFileExtension(storedFile.getFileExtension());
            noteFile.setDisplayOrder(i);
            noteFiles.add(noteFile);

            pendingUploads.add(new PendingUpload(
                    noteFile.getId(),
                    noteFile.getFilePath(),
                    file,
                    noteFile.getFileExtension(),
                    storedFile.isImage()));
        }

        if(noteFiles.size() > 0){
            noteFiles = noteFileRepository.saveAll(noteFiles);
            note.getFiles().addAll(noteFiles);

            registerPostCommitFileSync(pendingUploads);
        }

        return noteFiles;
    }

    private void registerPostCommitFileSync(List<PendingUpload> toUpload){
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                List<UUID> failed = new ArrayList<>();
                for(PendingUpload pendingUpload : toUpload){
                    try {
                        fileStorageService.finalizeUpload(pendingUpload);
                    } catch (Exception e) {
                        log.error("Failed to finalize upload for note file, path={}", pendingUpload.relativeOutputPath(), e);
                        failed.add(pendingUpload.noteFileId());
                    }
                }

                if(!failed.isEmpty()){
                    noteFileCleanupService.deleteOrphanedRows(failed);
                }
            }
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateNoteFiles(UUID userId, ItemNote note, List<UUID> removedFileIds, List<MultipartFile> files){

        List<String> filesToDelete = handleFileRemovals(userId, note, removedFileIds);

        int remainingCount = note.getFiles().size();
        validateAttachments(remainingCount, files);

        List<PendingUpload> pendingUploads = handleFileAdditions(note, files);

        applicationEventPublisher.publishEvent(new NoteFilesHardDeleteEvent(filesToDelete));
        registerPostCommitFileSync(pendingUploads);
    }


    /**
     * Rejects note attachments that exceed the configured count/size limits or whose
     * content-detected MIME type is not on the allow-list. Content is inspected with
     * Apache Tika; the client-supplied filename and {@code Content-Type} are ignored.
     *
     * @param existingCount number of attachments already on the note (post-removal)
     * @param files the newly uploaded attachments (may be {@code null}/empty)
     * @throws FieldValidationException if any file violates a constraint
     */
    private void validateAttachments(int existingCount, List<MultipartFile> files){
        if (files == null || files.isEmpty()) return;

        if (existingCount + files.size() > attachmentProperties.getMaxFilesPerNote()) {
            throw new FieldValidationException("files", "validation.too_many_files");
        }

        long maxBytes = attachmentProperties.getMaxFileSize().toBytes();
        List<FieldErrorDetail> errors = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                errors.add(new FieldErrorDetail("files", "validation.file_empty"));
                continue;
            }
            if (file.getSize() > maxBytes) {
                errors.add(new FieldErrorDetail("files", "validation.file_too_large"));
                continue;
            }
            String detectedMimeType = FileValidator.detectMimeType(file);
            if (!attachmentProperties.isAllowedMimeType(detectedMimeType)) {
                errors.add(new FieldErrorDetail("files", "validation.unsupported_file_type"));
            }
        }

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
    }

    private List<String> handleFileRemovals(UUID userId, ItemNote note, List<UUID> removedFileIds){
        List<UUID> safeIds = removedFileIds == null ? List.of() : removedFileIds;
        if (safeIds.isEmpty()) return List.of();

        List<NoteFile> filesToRemove = noteFileRepository.findAllByIdInAndUserIdAndNoteId(safeIds, userId, note.getId());

        Set<UUID> idsToRemove = filesToRemove.stream().map(NoteFile::getId).collect(Collectors.toSet());
        note.getFiles().removeIf(nf -> idsToRemove.contains(nf.getId()));

        return filesToRemove.stream().map(NoteFile::getFilePath).toList();
    }

    private List<PendingUpload> handleFileAdditions(ItemNote note, List<MultipartFile> newFiles){
        List<MultipartFile> safeFiles = newFiles == null ? List.of() : newFiles;
        if (safeFiles.isEmpty()) return List.of();

        int nextOrder = noteFileRepository.findMaxDisplayOrderByNoteId(note.getId()).orElse(-1) + 1;
        List<NoteFile> created = new ArrayList<>();
        List<PendingUpload> pendingUploads = new ArrayList<>();

        for(MultipartFile file: safeFiles){

            // generate soted file metadata
            UUID noteFileId = UUID.randomUUID();
            String detectedMimeType = FileValidator.detectMimeType(file);
            StoredFile storedFile = fileStorageService
                    .generateStoredFileMetadata(note.getUser().getId(), note.getItem().getId(),
                            note.getId(), noteFileId, file, detectedMimeType);

            // create note file entity
            NoteFile noteFile = new NoteFile();
            noteFile.setId(noteFileId);
            noteFile.setUser(note.getUser());
            noteFile.setNote(note);
            noteFile.setOriginalFilename(file.getOriginalFilename());
            noteFile.setStoredFilename(storedFile.getStoredFilename());
            noteFile.setFilePath(storedFile.getRelativeFilePath());
            noteFile.setMimeType(detectedMimeType);
            noteFile.setFileSize(file.getSize());
            noteFile.setFileExtension(storedFile.getFileExtension());
            noteFile.setDisplayOrder(nextOrder++);
            created.add(noteFile);

            pendingUploads.add(new PendingUpload(
                    noteFile.getId(),
                    noteFile.getFilePath(),
                    file,
                    noteFile.getFileExtension(),
                    storedFile.isImage()));
        }

        if(!created.isEmpty()){
            created = noteFileRepository.saveAll(created);
            note.getFiles().addAll(created);
        }

        return pendingUploads;
    }

}
