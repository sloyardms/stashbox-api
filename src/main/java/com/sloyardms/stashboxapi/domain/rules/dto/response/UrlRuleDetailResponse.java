package com.sloyardms.stashboxapi.domain.rules.dto.response;

import com.sloyardms.stashboxapi.domain.rules.model.Transform;
import com.sloyardms.stashboxapi.domain.stash.dto.response.ItemGroupRefResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UrlRuleDetailResponse {

    private UUID id;
    private ItemGroupRefResponse group;
    private String name;
    private String description;
    private String domain;
    private String urlPattern;
    private List<Transform> transforms;
    private Boolean active;
    private Integer priority;
    private Instant lastMatchedAt;
    private Instant createdAt;
    private Instant updatedAt;

}
