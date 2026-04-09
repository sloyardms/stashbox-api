package com.sloyardms.stashboxapi.domain.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateUserSettingsRequest {

    @NotNull(message = "{validation.notNull}")
    private Boolean darkModeEnabled;

    @NotNull(message = "{validation.notNull}")
    private Boolean filtersEnabled;

}
