package com.sloyardms.stashboxapi.domain.note.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UpdateItemNoteRequest {

    @Size(max = 1000, message = "validation.max")
    String content;

    Boolean pinned;

    List<UUID> removedFileIds;

    public UpdateItemNoteRequest(){
        removedFileIds = removedFileIds == null ? List.of() : removedFileIds;
    }

}
