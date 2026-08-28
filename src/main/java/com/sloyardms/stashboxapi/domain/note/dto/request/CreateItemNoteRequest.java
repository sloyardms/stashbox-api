package com.sloyardms.stashboxapi.domain.note.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateItemNoteRequest {

    @Size(max = 1000, message = "validation.max")
    String content;

}
