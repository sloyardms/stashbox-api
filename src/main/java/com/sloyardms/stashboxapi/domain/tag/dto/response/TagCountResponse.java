package com.sloyardms.stashboxapi.domain.tag.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TagCountResponse {

    private UUID id;
    private String name;
    private String slug;
    private Integer itemCount;

}
