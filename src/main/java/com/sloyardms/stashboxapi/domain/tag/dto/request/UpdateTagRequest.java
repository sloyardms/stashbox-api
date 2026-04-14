package com.sloyardms.stashboxapi.domain.tag.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateTagRequest {

    @NotBlank(message = "{validation.tag.name.notBlank}")
    @Size(max = 50, message = "{validation.tag.name.maxLength}")
    private String name;

}
