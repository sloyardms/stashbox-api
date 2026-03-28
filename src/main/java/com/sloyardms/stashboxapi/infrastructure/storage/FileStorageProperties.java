package com.sloyardms.stashboxapi.infrastructure.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.storage")
public class FileStorageProperties {

    private String basePath;
    private String usersDir;
    private String tempDir;
    private String logsDir;

    public Path usersPath() {
        return Path.of(basePath, usersDir);
    }

    public Path userPath(String userId) {
        return Path.of(basePath, usersDir, userId);
    }

    public Path userTempPath(String userId) {
        return Path.of(basePath, usersDir, userId, tempDir);
    }

}
