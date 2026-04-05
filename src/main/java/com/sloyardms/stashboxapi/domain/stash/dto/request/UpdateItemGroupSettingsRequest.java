package com.sloyardms.stashboxapi.domain.stash.dto.request;

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
public class UpdateItemGroupSettingsRequest {

    private boolean requiredTitle;
    private boolean uniqueTitle;
    private boolean requiredUrl;
    private boolean uniqueUrl;
    private boolean requiredImage;

}
