package com.sloyardms.stashboxapi.infrastructure.storage.listener;

import com.sloyardms.stashboxapi.infrastructure.storage.event.ImageHardDeleteEvent;
import com.sloyardms.stashboxapi.infrastructure.storage.event.StashItemHardDeleteEvent;
import com.sloyardms.stashboxapi.infrastructure.storage.event.UserHardDeleteEvent;
import com.sloyardms.stashboxapi.infrastructure.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileCleanupListener {

    private final FileStorageService fileStorageService;

    @Async("fileCleanupExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserDeleted(UserHardDeleteEvent event) {
        fileStorageService.deleteUserFolder(event.userId());
    }

    @Async("fileCleanupExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStashItemDeleted(StashItemHardDeleteEvent event) {
        fileStorageService.deleteStashItemFolder(event.userId(), event.stashItemId());
    }

    @Async("fileCleanupExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onImageDeleted(ImageHardDeleteEvent event) {
        fileStorageService.deleteFile(event.imagePath());
    }

}
