package com.sloyardms.stashboxapi.infrastructure.storage.event;

import java.util.UUID;

public record StashItemHardDeleteEvent(UUID userId, UUID stashItemId) {
}
