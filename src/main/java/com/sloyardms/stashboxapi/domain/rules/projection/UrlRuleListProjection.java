package com.sloyardms.stashboxapi.domain.rules.projection;

import java.time.Instant;
import java.util.UUID;

public interface UrlRuleListProjection {

    UUID getId();

    String getName();

    String getDomain();

    Boolean getActive();

    Integer getPriority();

    Instant getLastMatchedAt();

    Instant getCreatedAt();

    Instant getUpdatedAt();

    UUID getGroupId();

    String getGroupName();

}
