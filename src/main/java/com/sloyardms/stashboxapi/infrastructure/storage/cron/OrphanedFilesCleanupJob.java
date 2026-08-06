package com.sloyardms.stashboxapi.infrastructure.storage.cron;

import com.sloyardms.stashboxapi.domain.user.repository.UserRepository;
import com.sloyardms.stashboxapi.infrastructure.storage.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrphanedFilesCleanupJob {

    private static final Duration ORPHANED_FOLDER_TTL = Duration.ofHours(24);
    private static final Duration TEMP_FILE_TTL = Duration.ofHours(1);

    private final FileStorageProperties fileStorageProperties;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanOrphanedFolders() {
        log.info("Starting orphaned folders cleanup job");

        Path usersRoot = fileStorageProperties.getUsersPath();
        if (!Files.exists(usersRoot)) {
            log.info("Users storage root folder does not exist, skipping.");
            return;
        }

        try (Stream<Path> folders = Files.list(usersRoot)) {
            HashSet<String> existingUsersIds = userRepository.findAllUsersIds()
                    .stream()
                    .map(UUID::toString)
                    .collect(Collectors.toCollection(HashSet::new));

            List<Path> foldersToDelete = folders
                    .filter(Files::isDirectory)
                    .filter(folder -> isUUID(folder.getFileName().toString()))
                    .filter(folder -> !existingUsersIds.contains(folder.getFileName().toString()))
                    .filter(folder -> isOlderThan(folder, ORPHANED_FOLDER_TTL))
                    .toList();

            long deleted = foldersToDelete.stream()
                    .filter(this::deleteFolder)
                    .count();

            log.info("Orphaned folders cleanup complete — found {}, deleted {}",
                    foldersToDelete.size(), deleted);
        } catch (IOException e) {
            log.error("Orphaned folders cleanup job failed", e);
        }
    }

    @Scheduled(cron = "0 0 * * * *") // every hour
    public void cleanStaleTempFiles() {
        log.info("Starting stale temp files cleanup job");

        Path usersRoot = fileStorageProperties.getUsersPath();
        if (!Files.exists(usersRoot)) {
            log.info("Users storage root does not exist, skipping.");
            return;
        }

        try (Stream<Path> userFolders = Files.list(usersRoot)) {
            userFolders
                    .filter(Files::isDirectory)
                    .filter(folder -> isUUID(folder.getFileName().toString()))
                    .forEach(this::cleanUserTempFolder);
        } catch (IOException e) {
            log.error("Stale temp files cleanup job failed", e);
        }

        log.info("Stale temp files cleanup complete.");
    }

    private void cleanUserTempFolder(Path userFolder) {
        UUID userId = UUID.fromString(userFolder.getFileName().toString());
        Path tempDir = fileStorageProperties.getTempFilesPath(userId);

        if (!Files.exists(tempDir)) {
            return;
        }

        try (Stream<Path> files = Files.list(tempDir)) {
            List<Path> staleFiles = files
                    .filter(Files::isRegularFile)
                    .filter(file -> isOlderThan(file, TEMP_FILE_TTL))
                    .toList();

            long deleted = staleFiles.stream()
                    .filter(this::deleteFile)
                    .count();

            if (deleted > 0) {
                log.info("Deleted {}/{} stale temp file(s) for user {}",
                        deleted, staleFiles.size(), userId);
            }
        } catch (IOException e) {
            log.error("Failed to clean temp folder for user {}", userId, e);
        }
    }

    private boolean isUUID(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isOlderThan(Path path, Duration duration) {
        try {
            FileTime lastModified = Files.getLastModifiedTime(path);
            return lastModified.toInstant().isBefore(Instant.now().minus(duration));
        } catch (IOException e) {
            log.warn("Could not read last modified time for {}", path);
            return false;
        }
    }

    private boolean deleteFile(Path file) {
        try {
            Files.delete(file);
            return true;
        } catch (IOException e) {
            log.warn("Failed to delete temp file: {}", file, e);
            return false;
        }
    }

    private boolean deleteFolder(Path folder) {
        try (Stream<Path> paths = Files.walk(folder)) {
            paths.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (!file.delete()) {
                            log.warn("Failed to delete {}", file);
                        }
                    });

            log.info("Deleted orphaned folder: {}", folder);
            return true;
        } catch (IOException e) {
            log.error("Failed to delete folder: {}", folder, e);
            return false;
        }
    }
}