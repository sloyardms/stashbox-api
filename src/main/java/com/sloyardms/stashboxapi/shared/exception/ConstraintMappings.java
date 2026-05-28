package com.sloyardms.stashboxapi.shared.exception;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConstraintMappings {

    private static final Map<String, ConstraintInfo> CONSTRAINTS = Map.ofEntries(
            Map.entry("item_groups_user_id_slug_unique",
                    new ConstraintInfo("constraint.item_groups_user_id_slug_unique")),
            Map.entry("tags_user_id_group_id_slug_unique",
                    new ConstraintInfo("constraint.tags_user_id_group_id_slug_unique"))
    );

    public ConstraintInfo resolve(String constraintName) {
        return CONSTRAINTS.getOrDefault(constraintName.toLowerCase(), ConstraintInfo.UNKNOWN);
    }

}
