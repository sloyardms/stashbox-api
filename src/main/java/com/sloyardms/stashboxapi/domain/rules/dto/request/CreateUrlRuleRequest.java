package com.sloyardms.stashboxapi.domain.rules.dto.request;

import com.sloyardms.stashboxapi.domain.rules.model.Transform;
import com.sloyardms.stashboxapi.shared.validation.ValidRegex;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateUrlRuleRequest {

    @NotBlank(message = "validation.notBlank")
    @Size(max = 50, message = "validation.max")
    private String name;

    @Size(max = 255, message = "validation.max")
    private String description;

    @NotBlank(message = "validation.notBlank")
    @Size(max = 100, message = "validation.max")
    private String domain;

    @NotBlank(message = "validation.notBlank")
    @Size(max = 2048, message = "validation.max")
    @ValidRegex(message = "validation.invalidRegularExpression")
    private String urlPattern;

    private List<Transform> transforms = new ArrayList<>();

    @NotNull(message = "validation.notNull")
    @PositiveOrZero(message = "validation.positiveOrZero")
    private Integer priority;

}
