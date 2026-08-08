package com.sloyardms.stashboxapi.domain.stash.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class StashItemSummaryResponse {

    private UUID id;
    private String title;
    private String url;
    private String description;
    private String imagePath;
    private Boolean favorite;
    private Instant createdAt;
    private Instant updatedAt;

}
