package com.sloyardms.stashboxapi.domain.user.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
public class UserSettings {

    private boolean darkModeEnabled = false;
    private boolean filtersEnabled = false;

}
