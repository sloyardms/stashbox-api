package com.sloyardms.stashboxapi.domain.note.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class NoteFileResponse {

    UUID id;
    String originalFilename;
    String filePath;
    String mimeType;
    Long fileSize;
    Integer displayOrder;

}
