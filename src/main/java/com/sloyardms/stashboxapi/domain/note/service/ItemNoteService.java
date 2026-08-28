package com.sloyardms.stashboxapi.domain.note.service;

import com.sloyardms.stashboxapi.domain.note.dto.request.CreateItemNoteRequest;
import com.sloyardms.stashboxapi.domain.note.dto.request.UpdateItemNoteRequest;
import com.sloyardms.stashboxapi.domain.note.dto.response.ItemNoteDetailResponse;
import com.sloyardms.stashboxapi.domain.note.mapper.ItemNoteMapper;
import com.sloyardms.stashboxapi.domain.note.mapper.NoteFileMapper;
import com.sloyardms.stashboxapi.domain.note.model.ItemNote;
import com.sloyardms.stashboxapi.domain.note.model.NoteFile;
import com.sloyardms.stashboxapi.domain.note.repository.ItemNoteRepository;
import com.sloyardms.stashboxapi.domain.note.repository.NoteFileRepository;
import com.sloyardms.stashboxapi.domain.stash.model.StashItem;
import com.sloyardms.stashboxapi.domain.stash.repository.StashItemRepository;
import com.sloyardms.stashboxapi.infrastructure.storage.event.ItemNoteHardDeleteEvent;
import com.sloyardms.stashboxapi.shared.exception.types.ResourceNotFoundException;
import com.sloyardms.stashboxapi.shared.service.JsonPatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ItemNoteService {

    private final ItemNoteRepository itemNoteRepository;
    private final StashItemRepository stashItemRepository;
    private final ItemNoteMapper itemNoteMapper;
    private final NoteFileService noteFileService;
    private final NoteFileMapper noteFileMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final JsonPatchService jsonPatchService;
    private final NoteFileRepository noteFileRepository;

    @Transactional(readOnly = true)
    public Page<ItemNoteDetailResponse> list(UUID userId, String groupSlug, UUID itemId, Pageable pageable) {
        Pageable pageOnly = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        Page<ItemNote> notes = itemNoteRepository.findByUserIdAndGroupSlugItemId(userId, itemId, pageOnly);

        return notes.map(note -> {
            ItemNoteDetailResponse response = itemNoteMapper.toDetailResponse(note);
            response.setFiles(note.getFiles().stream().map(noteFileMapper::toResponse).toList());
            return response;
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public ItemNoteDetailResponse create(UUID userId, String groupSlug, UUID itemId, CreateItemNoteRequest request,
                                         List<MultipartFile> files) {
        StashItem stashItem = stashItemRepository.findByIdAndUserIdAndGroupSlug(itemId, userId, groupSlug)
                .orElseThrow(()-> new ResourceNotFoundException("StashItem", "id", itemId));

        // create the note
        UUID noteId = UUID.randomUUID();
        ItemNote itemNote = itemNoteMapper.toEntity(request);
        itemNote.setId(noteId);
        itemNote.setUser(stashItem.getUser());
        itemNote.setItem(stashItem);

        itemNote = itemNoteRepository.save(itemNote);

        // create note files
        List<NoteFile> noteFiles = noteFileService.createNoteFiles(itemNote, files);

        ItemNoteDetailResponse noteResponse = itemNoteMapper.toDetailResponse(itemNote);
        noteResponse.setFiles(noteFiles.stream().map(noteFileMapper::toResponse).toList());

        return noteResponse;
    }

    @Transactional(rollbackFor = Exception.class)
    public ItemNoteDetailResponse patch(UUID userId, String groupSlug, UUID itemId, UUID noteId, JsonNode patch,
                                        List<MultipartFile> files) {

        ItemNote targetNote = itemNoteRepository.findByIdAndUserIdAndItemId(noteId, userId, itemId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemNote", "id", noteId));

        UpdateItemNoteRequest updateDto = itemNoteMapper.toUpdateRequest(targetNote);
        UpdateItemNoteRequest patchedDto = jsonPatchService.applyPatch(patch, updateDto, UpdateItemNoteRequest.class);
        itemNoteMapper.updateEntityFromDto(patchedDto, targetNote);

        noteFileService.updateNoteFiles(userId, targetNote, patchedDto.getRemovedFileIds(), files);

        targetNote = itemNoteRepository.save(targetNote);

        ItemNoteDetailResponse response = itemNoteMapper.toDetailResponse(targetNote);
        response.setFiles(targetNote.getFiles().stream().map(noteFileMapper::toResponse).toList());
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(UUID userId, String groupSlug, UUID itemId, UUID noteId) {
        ItemNote note = itemNoteRepository.findByIdAndUserIdAndItemId(noteId, userId, itemId)
                .orElseThrow(()-> new ResourceNotFoundException("ItemNote", "id", noteId));

        itemNoteRepository.delete(note);
        applicationEventPublisher.publishEvent(new ItemNoteHardDeleteEvent(userId, itemId, noteId));
    }

}
