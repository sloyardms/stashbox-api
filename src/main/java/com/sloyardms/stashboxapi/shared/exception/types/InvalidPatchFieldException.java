package com.sloyardms.stashboxapi.shared.exception.types;

import lombok.Getter;

@Getter
public class InvalidPatchFieldException extends RuntimeException {

    private final String fieldName;

    public InvalidPatchFieldException(String fieldName) {
        super();
        this.fieldName = fieldName;
    }

}
