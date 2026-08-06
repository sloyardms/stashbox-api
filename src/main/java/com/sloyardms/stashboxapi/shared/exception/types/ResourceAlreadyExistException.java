package com.sloyardms.stashboxapi.shared.exception.types;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.CONFLICT)
public class ResourceAlreadyExistException extends RuntimeException {

    private final String constraintName;

    public ResourceAlreadyExistException(String constraintName) {
        super(constraintName);
        this.constraintName = constraintName;
    }

}
