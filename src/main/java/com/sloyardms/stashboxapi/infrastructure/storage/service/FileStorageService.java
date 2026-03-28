package com.sloyardms.stashboxapi.infrastructure.storage.service;

import com.sloyardms.stashboxapi.infrastructure.storage.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public void createUserFolder(UUID userId) {
        try {
            Files.createDirectories(fileStorageProperties.userPath(userId.toString()));
            Files.createDirectories(fileStorageProperties.userTempPath(userId.toString()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create folder for user " + userId, e);
        }
    }

    public void deleteUserFolder(UUID userId) {
        deleteFolder(fileStorageProperties.userPath(userId.toString()));
    }

    public void deleteFolder(Path folder) {
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
}
