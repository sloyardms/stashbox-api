package com.sloyardms.stashboxapi.domain.user.listener;

import com.sloyardms.stashboxapi.domain.user.event.UserDeletedEvent;
import com.sloyardms.stashboxapi.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserEventListener {

    private final UserService userService;

    @Async("webhookExecutor")
    @EventListener
    public void onDeleted(UserDeletedEvent event) {
        UUID userId = toUUID(event.userId());
        userService.delete(userId);
        log.info("Event processed");
    }

    private UUID toUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            log.error("Invalid provider ID format, skipping event processing: {}", id, e);
            throw new IllegalArgumentException("Invalid provider ID: " + id, e);
        }
    }
}
