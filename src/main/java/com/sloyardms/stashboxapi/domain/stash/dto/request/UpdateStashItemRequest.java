package com.sloyardms.stashboxapi.domain.stash.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class UpdateStashItemRequest {

    @Size(max = 255, message = "validation.max")
    private String title;

    @Size(max = 2048, message = "validation.max")
    private String url;

    @Size(max = 500, message = "validation.max")
    private String description;

    private Set<String> tags;

}
