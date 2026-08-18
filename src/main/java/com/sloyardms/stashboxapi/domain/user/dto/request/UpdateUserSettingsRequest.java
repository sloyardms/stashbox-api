package com.sloyardms.stashboxapi.domain.user.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateUserSettingsRequest {

    @NotNull(message = "validation.notNull}")
    private Boolean darkModeEnabled;

    @NotNull(message = "validation.notNull")
    private Boolean filtersEnabled;

    @Size(min=4, message = "validation.min")
    private String sheetPosition;

}
