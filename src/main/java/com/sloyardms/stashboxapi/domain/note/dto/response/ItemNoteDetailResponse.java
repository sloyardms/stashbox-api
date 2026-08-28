package com.sloyardms.stashboxapi.domain.note.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ItemNoteDetailResponse {

    UUID id;
    String content;
    Boolean pinned;
    List<NoteFileResponse> files;
    Instant createdAt;
    Instant updatedAt;

}
