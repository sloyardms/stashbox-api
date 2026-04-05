package com.sloyardms.stashboxapi.shared.exception.types;

public class MalformedRequestBodyException extends RuntimeException {

    public MalformedRequestBodyException(String message, Throwable cause) {
        super(message, cause);
    }

}
