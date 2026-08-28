package com.sloyardms.stashboxapi.infrastructure.storage.event;

import java.util.List;

public record NoteFilesHardDeleteEvent(List<String> relativeFilesPaths) {
}
