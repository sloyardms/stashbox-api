package com.sloyardms.stashboxapi.domain.rules.dto.response;

import com.sloyardms.stashboxapi.domain.rules.model.Transform;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UrlRuleSummaryResponse {

    private UUID id;
    private String name;
    private String domain;
    private String urlPattern;
    private List<Transform> transforms;
    private Integer priority;

}
