package com.sloyardms.stashboxapi.domain.stash.projection;

import java.time.Instant;
import java.util.UUID;

public interface StashItemSearchProjection {

    UUID getId();
    String getTitle();
    String getUrl();
    String getDescription();
    String getImagePath();
    Boolean getFavorite();
    Instant getCreatedAt();
    Instant getDeletedAt();

}
