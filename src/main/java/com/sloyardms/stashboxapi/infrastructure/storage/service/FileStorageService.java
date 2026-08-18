package com.sloyardms.stashboxapi.infrastructure.storage.service;

import com.sloyardms.stashboxapi.infrastructure.storage.FileStorageProperties;
import com.sloyardms.stashboxapi.infrastructure.storage.ImageProperties;
import com.sloyardms.stashboxapi.shared.utils.FileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
                .resolve(imageId + "." + imageProperties.getFormat());
        return writeImage(coverFile, file);
    }

    /**
     * Stores an arbitrary uploaded file, routing to image processing or raw byte copy
     * depending on the detected content type. Used by notes attachments (images and other files).
     *
     * @param outputFile the path to write the file to
     * @param file the file to copy
     * @return the path the file was written to
     * @throws IOException if the file can't be read or written
     * @throws IllegalStateException if the file's bytes can't be decoded as an image
     */
    private String upload(Path outputFile, MultipartFile file) throws IOException {
        // TODO: used by upcoming comment file/image uploads
        if (FileValidator.isImage(file)) {
            return writeImage(outputFile, file);
        } else {
            return writeFile(outputFile, file);
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
        return outputFile.toString();
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
        try (InputStream in = file.getInputStream()) {
            BufferedImage image = ImageIO.read(in);

            if (image == null) {
                throw new IllegalStateException("Could not read image — unsupported format or corrupt file");
            }

            Thumbnails.Builder<BufferedImage> thumbnailBuilder = Thumbnails.of(image)
                    .outputFormat(imageProperties.getFormat())
                    .outputQuality(imageProperties.getOutputQuality());

            int width = image.getWidth();
            if (width > imageProperties.getMaxWidth()) {
                thumbnailBuilder.width(imageProperties.getMaxWidth());
            } else {
                thumbnailBuilder.scale(1.0);
            }

            File fileToWrite = outputFile.toFile();
            thumbnailBuilder.toFile(fileToWrite);

            Path basePath = fileStorageProperties.getBasePath();
            Path fullPath = outputFile;

            // return relative path
            String imagePath = basePath.relativize(fullPath)
                    .toString()
                    .replace('\\', '/');

            return imagePath;
        }
    }

}
