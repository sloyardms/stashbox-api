package com.sloyardms.stashboxapi.domain.rules.model;

import jakarta.validation.constraints.NotNull;

public record ReplaceTransform(
        @NotNull(message = "validation.notNull") String from,
        @NotNull(message = "validation.notNull") String to) implements Transform {

    @Override
    public String apply(String input) {
        return input.replace(from, to);
    }

}
