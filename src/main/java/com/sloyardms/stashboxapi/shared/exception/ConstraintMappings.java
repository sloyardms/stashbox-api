package com.sloyardms.stashboxapi.shared.exception;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ConstraintMappings {

    private static final Map<String, ConstraintInfo> CONSTRAINTS = Map.ofEntries(
            Map.entry("item_groups_user_id_slug_unique",
                    new ConstraintInfo("constraint.item_groups_user_id_slug_unique", List.of("name"))),
            Map.entry("tags_user_id_group_id_slug_unique",
                    new ConstraintInfo("constraint.tags_user_id_group_id_slug_unique", List.of("name"))),
            Map.entry("stash_items_group_id_title_unique",
                    new ConstraintInfo("constraint.stash_items_unique_title", List.of("title"))),
            Map.entry("stash_items_group_id_url_unique",
                    new ConstraintInfo("constraint.stash_items_unique_url", List.of("url")))
    );

    public ConstraintInfo resolve(String constraintName) {
        return CONSTRAINTS.getOrDefault(constraintName.toLowerCase(), ConstraintInfo.UNKNOWN);
    }

}
