package com.sloyardms.stashboxapi.domain.rules.model;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public record DecodeTransform() implements Transform {

    @Override
    public String apply(String input) {
        try {
            return URLDecoder.decode(input, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return input;
        }
    }

}
