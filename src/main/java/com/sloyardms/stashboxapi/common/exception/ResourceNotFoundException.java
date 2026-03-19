package com.sloyardms.stashboxapi.common.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String identifier;
    private final Object value;

    public ResourceNotFoundException(String resourceType, String identifier, Object value) {
        super(resourceType);
        this.resourceType = resourceType;
        this.identifier = identifier;
        this.value = value;
    }

}
