package com.sloyardms.stashboxapi.domain.rules.model;

public record TrimTransform() implements Transform {

    @Override
    public String apply(String input) {
        return input.trim();
    }

}
