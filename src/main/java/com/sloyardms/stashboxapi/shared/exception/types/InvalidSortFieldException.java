package com.sloyardms.stashboxapi.shared.exception.types;

import lombok.Getter;

import java.util.Set;

@Getter
public class InvalidSortFieldException extends RuntimeException {

    private final String invalidField;
    private final Set<String> allowedFields;

    public InvalidSortFieldException(String invalidField, Set<String> allowedFields) {
        super(invalidField);
        this.invalidField = invalidField;
        this.allowedFields = allowedFields;
    }

}
