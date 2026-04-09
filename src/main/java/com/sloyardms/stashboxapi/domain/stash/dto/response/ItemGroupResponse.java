package com.sloyardms.stashboxapi.domain.stash.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ItemGroupResponse {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String icon;
    private Boolean defaultGroup;
    private Integer position;

}
