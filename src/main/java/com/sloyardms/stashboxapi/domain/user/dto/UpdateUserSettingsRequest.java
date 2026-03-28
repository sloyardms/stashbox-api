package com.sloyardms.stashboxapi.domain.user.dto;

import com.sloyardms.stashboxapi.shared.validation.AtLeastOneNonNullField;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AtLeastOneNonNullField
public class UpdateUserSettingsRequest {

    private Boolean darkMode;
    private Boolean useFilters;

}
