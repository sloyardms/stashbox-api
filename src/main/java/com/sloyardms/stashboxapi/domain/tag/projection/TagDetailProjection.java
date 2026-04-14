package com.sloyardms.stashboxapi.domain.tag.projection;

import java.time.Instant;
import java.util.UUID;

/**
 * Projection used with native queries
 */
public interface TagDetailProjection {

    UUID getId();

    String getName();

    String getSlug();

    Instant getCreatedAt();

    Instant getUpdatedAt();

    Integer getItemCount();

    Instant getLastUsed();

}
