package com.sloyardms.stashboxapi.shared.exception.types;

import lombok.Getter;

@Getter
public class DuplicateResourceException extends RuntimeException {

    private final String field;
    private final Object value;

    public DuplicateResourceException(String field,  Object value) {
        this.field = field;
        this.value = value;
    }

}
