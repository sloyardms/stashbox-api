package com.sloyardms.stashboxapi.infrastructure.storage.event;

import java.util.List;
import java.util.UUID;

public record StashItemsHardDeleteEvent(UUID userId, List<UUID> stashItemsIds) {
}
