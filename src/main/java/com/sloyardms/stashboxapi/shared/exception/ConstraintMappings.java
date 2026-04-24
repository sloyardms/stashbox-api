package com.sloyardms.stashboxapi.shared.exception;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConstraintMappings {

    private static final Map<String, ConstraintInfo> CONSTRAINTS = Map.of(
            // --- item_groups ---
            "item_groups_slug_unique",
            new ConstraintInfo("constraint.item_groups_slug_unique", "name"),

            // --- url_rules ---
            "url_rules_user_name_unique",
            new ConstraintInfo("constraint.url_rules_user_name_unique", "name"),
            "url_rules_user_url_pattern_unique",
            new ConstraintInfo("constraint.url_rules_user_url_pattern_unique", "urlPattern"),

            // --- tags ---
            "tags_slug_unique",
            new ConstraintInfo("constraint.tags_slug_unique", "name")
    );

    public ConstraintInfo resolve(String constraintName) {
        return CONSTRAINTS.getOrDefault(constraintName.toLowerCase(), ConstraintInfo.UNKNOWN);
    }

}
