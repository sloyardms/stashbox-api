package com.sloyardms.stashboxapi.domain.stash.dto.response;

import com.sloyardms.stashboxapi.domain.tag.dto.response.TagCountResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class StashItemDetailResponse {

    private UUID id;
    private ItemGroupRefResponse group;
    private String title;
    private String titleNormalized;
    private String url;
    private String urlNormalized;
    private String description;
    private String imagePath;
    private Boolean favorite;
    private Instant deletedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private List<TagCountResponse> tags;

}
