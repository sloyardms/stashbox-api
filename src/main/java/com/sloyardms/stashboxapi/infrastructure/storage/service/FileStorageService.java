package com.sloyardms.stashboxapi.infrastructure.storage.service;

import com.sloyardms.stashboxapi.infrastructure.storage.FileStorageProperties;
import com.sloyardms.stashboxapi.infrastructure.storage.ImageProperties;
import com.sloyardms.stashboxapi.infrastructure.storage.PendingUpload;
import com.sloyardms.stashboxapi.infrastructure.storage.StoredFile;
import com.sloyardms.stashboxapi.shared.utils.FileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageProperties fileStorageProperties;
    private final ImageProperties imageProperties;

    public String resolveImagePath(String relativeImagePath){
        return fileStorageProperties.getBasePath().resolve(relativeImagePath).toString();
    }

    public void deleteBaseFolder(){
        deleteFolder(fileStorageProperties.getBasePath());
    }

    public void deleteUserFolder(UUID userId) {
        deleteFolder(fileStorageProperties.getUserPath(userId));
    }

    public void deleteStashItemFolder(UUID userId, UUID stashItemId) {
        Path folder = fileStorageProperties.getStashItemPath(userId, stashItemId);
        deleteFolder(folder);
    }

    public void deleteItemNoteFolder(UUID userId, UUID itemId, UUID noteId) {
        Path folder = fileStorageProperties.getNoteFilePath(userId, itemId, noteId);
        deleteFolder(folder);
    }

    public void deleteFile(String filePath) {
        Path file = fileStorageProperties.getFilePathFromRelativePath(filePath);
        if(!Files.exists(file)){
            log.warn("File does not exist, skipping deletion: {}", filePath);
            return;
        }

        try{
            Files.delete(file);
        } catch (IOException e){
            log.error("Failed to delete {}: {}", file, e.getMessage());
        }
    }

    private void deleteFolder(Path folder) {
        if (!Files.exists(folder)) {
            log.debug("Folder does not exist, skipping deletion: {}", folder);
            return;
        }

        boolean success = true;

        try (Stream<Path> paths = Files.walk(folder)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    success = false;
                    log.error("Failed to delete {}", path, e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            success = false;
            log.error("Failed to traverse folder {}", folder, e.getMessage(), e);
        }

        if (success) {
            log.info("Deleted folder: {}", folder);
        } else {
            log.warn("Folder cleanup incomplete: {}", folder);
        }
    }

    /**
     * Stores the cover image for a StashItem, always processing it as an image
     * (resize/reformat via Thumbnailator) regardless of the uploaded content type.
     *
     * @param userId the user who owns the StashItem
     * @param stashItemId the id of the StashItem
     * @param imageId the id of the image
     * @param file the file to process
     * @return the path the image was written to
     * @throws IOException if the file can't be read or written
     * @throws IllegalStateException if the file's bytes can't be decoded as an image
     */
    public String uploadCover(UUID userId, UUID stashItemId, UUID imageId, MultipartFile file) throws IOException,
            IllegalStateException {
        Path coverFile = fileStorageProperties
                .getCoverPath(userId, stashItemId)
                .resolve(imageId + "." + getFileExtension(file));
        return writeImage(coverFile, file);
    }

    /**
     * Stores an arbitrary uploaded file, routing to image processing or raw byte copy
     * depending on the detected content type. Used by notes attachments (images and other files).
     *
     * @param pendingUpload file to be uploaded after creating its row in the db
     * @throws IOException if the file can't be read or written
     * @throws IllegalStateException if the file's bytes can't be decoded as an image
     */
    public void finalizeUpload(PendingUpload pendingUpload) throws IOException {
        Path fileOutputPath = fileStorageProperties
                .getBasePath()
                .resolve(pendingUpload.relativeOutputPath());

        if(pendingUpload.isImage()){
            writeImage(fileOutputPath, pendingUpload.file());
        }else{
            writeFile(fileOutputPath, pendingUpload.file());
        }
    }

    /**
     * Copies the file's bytes as-is to {@code outputFile}, creating parent directories
     *
     * @param outputFile the path to write the file to
     * @param file the file to copy
     * @return the path the file was written to
     * @throws IOException if the file can't be read or written
     */
    private String writeFile(Path outputFile, MultipartFile file) throws IOException {
        Files.createDirectories(outputFile.getParent());
        Files.copy(file.getInputStream(), outputFile);

        Path basePath = fileStorageProperties.getBasePath();
        Path fullPath = outputFile;

        // return relative path
        String filePath = basePath.relativize(fullPath)
                .toString()
                .replace('\\', '/');
        return filePath;
    }

    /**
     * Decodes, resizes (if wider than the configured max), and re-encodes the image
     * to the configured output format/quality, writing the result to {@code outputFile}
     *
     * @param outputFile the path to write the processed image to
     * @param file the file to process
     * @return the path the processed image was written to
     * @throws IOException if the file can't be read or written
     * @throws IllegalStateException if the file's bytes can't be decoded as an image
     */
    private String writeImage(Path outputFile, MultipartFile file) throws IOException {
        Files.createDirectories(outputFile.getParent());

        BufferedImage image;

        try (InputStream in = file.getInputStream()) {
            image = ImageIO.read(in);
        }

        if (image == null) {
            throw new IllegalStateException(
                    "Could not read image — unsupported format or corrupt file"
            );
        }

        int maxSize = imageProperties.getMaxSize();

        // Image already fits, preserve the original file as-is.
        if (image.getWidth() <= maxSize && image.getHeight() <= maxSize) {
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, outputFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            // Image is too large , resize while preserving aspect ratio.
            Thumbnails.Builder<BufferedImage> builder = Thumbnails.of(image)
                    .size(maxSize, maxSize)
                    .keepAspectRatio(true);

            float quality = imageProperties.getOutputQuality();
            if (quality != 0.0f) {
                builder.outputQuality(quality);
            }

            builder.toFile(outputFile.toFile());
        }

        return fileStorageProperties.getBasePath()
                .relativize(outputFile)
                .toString()
                .replace('\\', '/');
    }

    public StoredFile generateStoredFileMetadata(UUID userId, UUID itemId, UUID noteId, UUID fileId, MultipartFile file,
                                                String detectedMimeType){
        StoredFile storedFile = new StoredFile();

        storedFile.setImage(FileValidator.isImage(detectedMimeType));
        storedFile.setFileExtension(resolveSafeExtension(detectedMimeType, file));

        Path relativeNoteFilePath = fileStorageProperties
                .getRelativeNoteFilePath(userId, itemId, noteId)
                .resolve(fileId + "." +storedFile.getFileExtension());

        storedFile.setRelativeFilePath(relativeNoteFilePath.toString());
        storedFile.setStoredFilename(fileId.toString()+"."+storedFile.getFileExtension());

        return storedFile;
    }

    private String getFileExtension(MultipartFile file){
        String fileName = file.getOriginalFilename();
        String extension = "";
        if (fileName != null && fileName.contains(".")) {
            extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return extension;
    }

    /**
     * Derives the stored-file extension from the <em>content-detected</em> MIME type so the
     * on-disk name can never carry a client-chosen, dangerous extension (e.g. an image
     * polyglot named {@code evil.html}). Falls back to a sanitised extension from the
     * original filename, and finally to {@code bin}.
     */
    private String resolveSafeExtension(String detectedMimeType, MultipartFile file) {
        if (detectedMimeType != null && !detectedMimeType.isBlank()) {
            try {
                String ext = MimeTypes.getDefaultMimeTypes()
                        .forName(detectedMimeType)
                        .getExtension();
                if (ext != null && !ext.isBlank()) {
                    return ext.startsWith(".") ? ext.substring(1) : ext;
                }
            } catch (MimeTypeException e) {
                log.debug("No known extension for MIME type {}", detectedMimeType);
            }
        }

        String raw = getFileExtension(file);
        return raw.matches("[A-Za-z0-9]{1,12}") ? raw.toLowerCase() : "bin";
    }

}
