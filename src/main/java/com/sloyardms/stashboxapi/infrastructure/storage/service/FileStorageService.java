package com.sloyardms.stashboxapi.infrastructure.storage.service;

import com.sloyardms.stashboxapi.infrastructure.storage.FileStorageProperties;
import com.sloyardms.stashboxapi.infrastructure.storage.ImageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

    private void deleteFolder(Path folder) {
        if (!Files.exists(folder)) {
            log.warn("Folder does not exist, skipping deletion: {}", folder);
            return;
        }

        try (Stream<Path> paths = Files.walk(folder)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.error("Failed to delete {}: {}", path, e.getMessage());
                        }
                    });
            log.info("Deleted folder: {}", folder);
        } catch (IOException e) {
            log.error("Failed to traverse folder {}: {}", folder, e.getMessage());
        }
    }

    public String uploadCover(UUID userId, UUID stashItemId, UUID imageId, MultipartFile file) throws IOException,
            IllegalStateException {
        Path coverFile = fileStorageProperties
                .getCoverPath(userId, stashItemId)
                .resolve(imageId + "." + imageProperties.getFormat());
        return upload(coverFile, file);
    }

    private String upload(Path outputFile, MultipartFile file) throws IOException {
        if (isImage(file)) {
            return writeImage(outputFile, file);
        } else {
            return writeFile(outputFile, file);
        }
    }

    private String writeFile(Path outputFile, MultipartFile file) throws IOException {
        Files.createDirectories(outputFile.getParent());
        Files.copy(file.getInputStream(), outputFile);
        return outputFile.toString();
    }

    private String writeImage(Path outputFile, MultipartFile file) throws IOException {
        Files.createDirectories(outputFile.getParent());
        BufferedImage image = ImageIO.read(file.getInputStream());

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
        return outputFile.toString();
    }

    private boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
}
