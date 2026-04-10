package com.sloyardms.stashboxapi.domain.tag.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TagDetailResponse {

    private UUID id;
    private String name;
    private String slug;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer itemCount;
    private Instant lastUsed;

}
