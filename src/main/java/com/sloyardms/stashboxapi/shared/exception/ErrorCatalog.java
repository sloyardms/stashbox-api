package com.sloyardms.stashboxapi.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.net.URI;

@Getter
public enum ErrorCatalog {

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
            "error.server.internal.title",
            "error.server.internal.detail",
            "urn:stashbox:error:internal-error"),

    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT,
            "error.server.dataIntegrityViolation.title",
            "error.server.dataIntegrityViolation.detail",
            "urn:stashbox:error:data-integrity-violation"),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,
            "error.auth.unauthorized.title",
            "error.auth.unauthorized.detail",
            "urn:stashbox:error:unauthorized"),

    FORBIDDEN(HttpStatus.FORBIDDEN,
            "error.auth.forbidden.title",
            "error.auth.forbidden.detail",
            "urn:stashbox:error:forbidden"),

    MISSING_REQUIRED_PARAMETER(HttpStatus.BAD_REQUEST,
            "error.request.missingParameter.title",
            "error.request.missingParameter.detail",
            "urn:stashbox:error:missing-required-parameter"),

    REQUEST_MISSING_HEADER(HttpStatus.BAD_REQUEST,
            "error.request.missingHeader.title",
            "error.request.missingHeader.detail",
            "urn:stashbox:error:missing-header"),

    REQUEST_INVALID_PARAMETER_TYPE(HttpStatus.BAD_REQUEST,
            "error.request.invalidParameterType.title",
            "error.request.invalidParameterType.detail",
            "urn:stashbox:error:invalid-parameter-type"),

    VALIDATION_ERROR(HttpStatus.UNPROCESSABLE_CONTENT,
            "error.validation.failed.title",
            "error.validation.failed.detail",
            "urn:stashbox:error:validation-failed"),

    PAGEABLE_INVALID_SORT_FIELD(HttpStatus.UNPROCESSABLE_CONTENT,
            "error.validation.invalidSortField.title",
            "error.validation.invalidSortField.detail",
            "urn:stashbox:error:invalid-sort-field"),

    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND,
            "error.resource.notFound.title",
            "error.resource.notFound.detail",
            "urn:stashbox:error:resource-not-found"),

    DUPLICATE_RESOURCE(HttpStatus.CONFLICT,
            "error.resource.duplicate.title",
            "error.resource.duplicate.detail",
            "urn:stashbox:error:duplicate-resource"),

    ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND,
            "error.endpoint.notFound.title",
            "error.endpoint.notFound.detail",
            "urn:stashbox:error:endpoint-not-found"),

    HTTP_METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED,
            "error.endpoint.methodNotAllowed.title",
            "error.endpoint.methodNotAllowed.detail",
            "urn:stashbox:error:method-not-allowed"),

    MEDIA_TYPE_NOT_SUPPORTED(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "error.media.notSupported.title",
            "error.media.notSupported.detail",
            "urn:stashbox:error:unsupported-media-type"),

    MEDIA_TYPE_NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE,
            "error.media.notAcceptable.title",
            "error.media.notAcceptable.detail",
            "urn:stashbox:error:media-type-not-acceptable"),

    UPLOAD_SIZE_EXCEEDED(HttpStatus.CONTENT_TOO_LARGE,
            "error.upload.sizeLimitExceeded.title",
            "error.upload.sizeLimitExceeded.detail",
            "urn:stashbox:error:upload-size-exceeded"),

    MALFORMED_MULTIPART_REQUEST(HttpStatus.BAD_REQUEST,
            "error.upload.malformedMultipart.title",
            "error.upload.malformedMultipart.detail",
            "urn:stashbox:error:malformed-multipart-request"),

    DEFAULT_GROUP_DELETION_NOT_ALLOWED(HttpStatus.BAD_REQUEST,
            "error.group.default.deletionNotAllowed.title",
            "error.group.default.deletionNotAllowed.detail",
            "urn:stashbox:error:default-group-deletion-notallowed"
    ),

    MALFORMED_REQUEST_BODY(HttpStatus.BAD_REQUEST,
            "error.request.malformedBody.title",
            "error.request.malformedBody.detail",
            "urn:stashbox:error:malformed-request-body"),

    EMPTY_PATCH_BODY(HttpStatus.BAD_REQUEST,
            "error.patch.emptyBody.title",
            "error.patch.emptyBody.detail",
            "urn:stashbox:error:empty-patch-body"),

    INVALID_PATCH_FIELD_TYPE(HttpStatus.BAD_REQUEST,
            "error.patch.invalidFieldType.title",
            "error.patch.invalidFieldType.detail",
            "urn:stashbox:error:invalid-patch-field"),

    NOT_A_JSON_OBJECT(HttpStatus.BAD_REQUEST,
            "error.patch.notAnObject.title",
            "error.patch.notAnObject.detail",
            "urn:stashbox:error:not-a-json-object");

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