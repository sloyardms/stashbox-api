package com.sloyardms.stashboxapi.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class UserSettingsResponse {

    private Boolean darkModeEnabled;
    private Boolean filtersEnabled;

}
