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

    private final FileStorageProperties fileStorageProperties;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanOrphanedFolders() {
        log.info("Starting orphaned folders cleanup job");

        Path usersRoot = Path.of(fileStorageProperties.getBasePath());

        if (!Files.exists(usersRoot)) {
            log.info("Storage root does not exist, skipping.");
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
                    .filter(folder -> isFolderOlderThan(folder, Duration.ofHours(24)))
                    .toList();

            long deletedFolders = foldersToDelete.stream()
                    .filter(this::deleteFolder)
                    .count();

            log.info("Found {} orphaned folder(s), deleted {}", foldersToDelete.size(), deletedFolders);
            log.info("Orphaned folders cleanup job complete.");
        } catch (IOException e) {
            log.error("Orphaned folders cleanup job failed", e);
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

    private boolean isFolderOlderThan(Path folder, Duration duration) {
        try {
            FileTime created = Files.getLastModifiedTime(folder);
            return created.toInstant().isBefore(Instant.now().minus(duration));
        } catch (IOException e) {
            log.warn("Could not read creation time for {}", folder);
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