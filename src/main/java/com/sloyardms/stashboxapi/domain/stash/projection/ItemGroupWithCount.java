package com.sloyardms.stashboxapi.domain.stash.projection;

import java.util.UUID;

public interface ItemGroupWithCount {

    UUID getId();

    String getName();

    String getDescription();

    String getSlug();

    String getIcon();

    boolean isDefaultGroup();

    Integer getPosition();

    Long getItemCount();

}
