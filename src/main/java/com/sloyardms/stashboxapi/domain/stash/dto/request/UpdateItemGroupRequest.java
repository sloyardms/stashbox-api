package com.sloyardms.stashboxapi.domain.stash.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
public class UpdateItemGroupRequest {

    @NotBlank(message = "validation.notBlank")
    @Length(max = 75, message = "validation.max")
    private String name;

    @Length(max = 255, message = "validation.max")
    private String description;

    @Length(max = 50, message = "validation.max")
    private String icon;

    @Valid
    private UpdateItemGroupSettingsRequest settings;

}
