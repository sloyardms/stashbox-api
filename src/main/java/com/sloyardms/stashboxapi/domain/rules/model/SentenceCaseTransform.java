package com.sloyardms.stashboxapi.domain.rules.model;

public record SentenceCaseTransform() implements Transform {

    @Override
    public String apply(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

}
