package com.sloyardms.stashboxapi.domain.user.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class UserSettings {

    private boolean darkModeEnabled = false;
    private boolean filtersEnabled = false;

}
