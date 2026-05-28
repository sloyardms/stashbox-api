package com.sloyardms.stashboxapi.shared.exception.types;

public class InvalidRegexException extends RuntimeException {

    public InvalidRegexException(String pattern) {
        super(pattern);
    }

}
