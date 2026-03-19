package com.sloyardms.stashboxapi.domain.stash.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ItemGroupSettings {

    private boolean requiredTitle = false;
    private boolean uniqueTitle = false;
    private boolean requiredUrl = false;
    private boolean uniqueUrl = false;
    private boolean requiredImage = false;

}
