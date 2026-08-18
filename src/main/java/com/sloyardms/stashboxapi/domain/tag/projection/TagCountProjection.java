package com.sloyardms.stashboxapi.domain.tag.projection;

import java.time.Instant;
import java.util.UUID;

/**
 * Projection used with native queries
 */
public interface TagCountProjection {

    UUID getId();

    String getName();

    String getSlug();

    Integer getItemCount();

    Instant getCreatedAt();

    Instant getUpdatedAt();

    Instant getLastUsed();

}
