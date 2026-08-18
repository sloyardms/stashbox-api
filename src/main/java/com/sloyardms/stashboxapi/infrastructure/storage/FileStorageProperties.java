package com.sloyardms.stashboxapi.infrastructure.storage;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Setter
@Component
@ConfigurationProperties(prefix = "app.storage")
public class FileStorageProperties {

    private String basePath;
    private String usersDir;
    private String tempDir;
    private String itemsDir;
    private String coverDir;
    private String commentsDir;

    public Path getBasePath(){
        return Paths.get(basePath);
    }

    public Path getUsersPath() {
        return Path.of(basePath, usersDir);
    }

    public Path getUserPath(UUID userId) {
        return Path.of(basePath, usersDir, userId.toString());
    }

    public Path getTempFilesPath(UUID userId) {
        return Path.of(basePath, userId.toString(), tempDir);
    }

    public Path getStashItemPath(UUID userId, UUID itemId) {
        return Path.of(basePath, usersDir, userId.toString(), itemsDir, itemId.toString());
    }

    public Path getCoverPath(UUID userId, UUID itemId) {
        return Path.of(basePath, usersDir, userId.toString(), itemsDir, itemId.toString(), coverDir);
    }

    public Path getFilePathFromRelativePath(String relativePath){
        return Path.of(basePath, relativePath);
    }

}
