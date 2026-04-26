package com.sloyardms.stashboxapi.domain.rules.dto.request;

import com.sloyardms.stashboxapi.domain.rules.model.Transform;
import com.sloyardms.stashboxapi.shared.validation.ValidRegex;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UpdateUrlRuleRequest {

    private UUID groupId;

    @Size(min = 1, max = 50, message = "validation.size")
    private String name;

    @Size(max = 255, message = "validation.max")
    private String description;

    @Size(min = 1, max = 100, message = "validation.size")
    private String domain;

    @Size(min = 1, max = 2048, message = "validation.size")
    @ValidRegex(message = "validation.invalidRegularExpression")
    private String urlPattern;

    private List<Transform> transforms;

    private Boolean active;

    @PositiveOrZero(message = "validation.positiveOrZero")
    private Integer priority;

}
