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

    @NotBlank(message = "{validations.itemGroup.name.notBlank}")
    @Length(max = 75, message = "{validations.itemGroup.name.maxLength}")
    private String name;

    @Length(max = 255, message = "{validations.itemGroup.description.maxLength}")
    private String description;

    @Length(max = 50, message = "{validations.itemGroup.icon.maxLength}")
    private String icon;

    @Valid
    private CreateItemGroupSettingsRequest settings;

}
