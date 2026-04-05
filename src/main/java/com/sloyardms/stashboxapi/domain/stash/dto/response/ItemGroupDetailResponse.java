package com.sloyardms.stashboxapi.domain.stash.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ItemGroupDetailResponse {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String icon;
    private Boolean defaultGroup;
    private ItemGroupSettingsResponse settings;
    private Integer position;
    private Instant createdAt;
    private Instant updatedAt;

}
