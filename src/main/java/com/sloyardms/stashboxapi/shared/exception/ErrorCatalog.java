package com.sloyardms.stashboxapi.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.net.URI;

@Getter
public enum ErrorCatalog {

    INTERNAL_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "error.generic.internal_error.title",
            "error.generic.internal_error.detail",
            "urn:stashbox:error:internal-error"
    ),
    FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "error.auth.forbidden.title",
            "error.auth.forbidden.detail",
            "urn:stashbox:error:forbidden"
    ),
    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "error.auth.unauthorized.title",
            "error.auth.unauthorized.detail",
            "urn:stashbox:error:unauthorized"
    ),
    PAGEABLE_INVALID_SORT_FIELD(
            HttpStatus.BAD_REQUEST,
            "error.pagination.invalid_sort_field.title",
            "error.pagination.invalid_sort_field.detail",
            "urn:stashbox:error:invalid-sort-field"
    ),
    RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "error.resource.not_found.title",
            "error.resource.not_found.detail",
            "urn:stashbox:error:resource-not-found"
    ),
    REQUEST_INVALID_PARAMETER_TYPE(
            HttpStatus.BAD_REQUEST,
            "error.validation.invalid_parameter_type.title",
            "error.validation.invalid_parameter_type.detail",
            "urn:stashbox:error:invalid-parameter-type"
    ),
    ENDPOINT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "error.endpoint.not_found.title",
            "error.endpoint.not_found.detail",
            "urn:stashbox:error:endpoint-not-found"
    ),
    HTTP_METHOD_NOT_ALLOWED(
            HttpStatus.METHOD_NOT_ALLOWED,
            "error.endpoint.method-not-allowed.title",
            "error.endpoint.method-not-allowed.detail",
            "urn:stashbox:error:method-not-allowed"
    ),
    VALIDATION_ERROR(
            HttpStatus.BAD_REQUEST,
            "error.validation.failed.title",
            "error.validation.failed.detail",
            "urn:stashbox:error:validation-failed"
    ),
    MALFORMED_REQUEST_BODY(
            HttpStatus.BAD_REQUEST,
            "error.validation.malformed-request-body.title",
            "error.validation.malformed-request-body.detail",
            "urn:stashbox:error:malformed-request-body"
    );

    private final HttpStatus status;
    private final String titleKey;
    private final String detailKey;
    private final URI type;

    ErrorCatalog(HttpStatus status, String titleKey, String detailKey, String type) {
        this.status = status;
        this.titleKey = titleKey;
        this.detailKey = detailKey;
        this.type = URI.create(type);
    }

}