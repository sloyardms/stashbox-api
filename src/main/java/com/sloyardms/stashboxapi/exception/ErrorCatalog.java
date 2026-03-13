package com.sloyardms.stashboxapi.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.net.URI;

@Getter
public enum ErrorCatalog {

    FORBIDDEN(
            HttpStatus.FORBIDDEN,
            MessageKeys.Error.Auth.FORBIDDEN_TITLE,
            MessageKeys.Error.Auth.FORBIDDEN_DETAIL
    ),
    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            MessageKeys.Error.Auth.UNAUTHORIZED_TITLE,
            MessageKeys.Error.Auth.UNAUTHORIZED_DETAIL
    );

    private static final String URN_PREFIX = "urn:stashbox:error:";
    private final HttpStatus status;
    private final String title;
    private final String detail;


    ErrorCatalog(HttpStatus status, String title) {
        this.status = status;
        this.title = title;
        this.detail = null;
    }

    ErrorCatalog(HttpStatus status, String title, String detail) {
        this.status = status;
        this.title = title;
        this.detail = detail;
    }

    public URI getType() {
        return URI.create(URN_PREFIX + name()
                .toLowerCase()
                .replace("_", "-"));
    }

    public String getDetail() {
        return detail != null ? detail : title; // fallback
    }

}