package com.sloyardms.stashboxapi.shared.utils;

import com.github.slugify.Slugify;

import java.text.Normalizer;
import java.util.Locale;

public class SlugUtils {

    private static final Slugify SLUGIFY = Slugify.builder()
            .transliterator(true)
            .locale(Locale.ENGLISH)
            .build();

    private SlugUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String slugify(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return SLUGIFY.slugify(text);
    }

    public static String normalize(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        String unicodeNormalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        String withoutAccents = unicodeNormalized.replaceAll("\\p{M}", "");
        String lowercase = withoutAccents.toLowerCase(Locale.ROOT);
        return lowercase.trim().replaceAll("\\s+", " ");
    }

}
