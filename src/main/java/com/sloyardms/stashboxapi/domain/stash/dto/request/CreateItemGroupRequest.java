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
public class CreateItemGroupRequest {

    @NotBlank(message = "{validation.itemGroup.name.notBlank}")
    @Length(max = 75, message = "{validation.itemGroup.name.maxLength}")
    private String name;

    @Length(max = 255, message = "{validation.itemGroup.description.maxLength}")
    private String description;

    @Length(max = 50, message = "{validation.itemGroup.icon.maxLength}")
    private String icon;

    @Valid
    private CreateItemGroupSettingsRequest settings;

}
