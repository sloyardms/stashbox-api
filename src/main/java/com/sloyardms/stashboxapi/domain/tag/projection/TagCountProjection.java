package com.sloyardms.stashboxapi.domain.tag.projection;

import java.util.UUID;

/**
 * Projection used with native queries
 */
public interface TagCountProjection {

    UUID getId();

    String getName();

    String getSlug();

    Integer getItemCount();

}
