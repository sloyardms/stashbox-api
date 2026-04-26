package com.sloyardms.stashboxapi.domain.rules.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DecodeTransform.class, name = "decode"),
        @JsonSubTypes.Type(value = TrimTransform.class, name = "trim"),
        @JsonSubTypes.Type(value = SentenceCaseTransform.class, name = "sentenceCase"),
        @JsonSubTypes.Type(value = ReplaceTransform.class, name = "replace"),
})
public sealed interface Transform permits DecodeTransform, TrimTransform, SentenceCaseTransform, ReplaceTransform {

    String apply(String input);

}
