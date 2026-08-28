package com.sloyardms.stashboxapi.infrastructure.storage.event;

import java.util.UUID;

public record ItemNoteHardDeleteEvent(UUID userId, UUID itemId, UUID noteId) {
}
