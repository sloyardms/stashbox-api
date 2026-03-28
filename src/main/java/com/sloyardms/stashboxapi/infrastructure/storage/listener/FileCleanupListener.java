package com.sloyardms.stashboxapi.infrastructure.storage.listener;

import com.sloyardms.stashboxapi.infrastructure.storage.event.UserFolderDeleteEvent;
import com.sloyardms.stashboxapi.infrastructure.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileCleanupListener {

    private final FileStorageService fileStorageService;

    @Async("fileCleanupExecutor")
    @EventListener
    public void onUserFolderDeleted(UserFolderDeleteEvent event) {
        fileStorageService.deleteUserFolder(event.userId());
    }
}
