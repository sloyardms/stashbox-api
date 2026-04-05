package com.sloyardms.stashboxapi.shared.exception;

import com.sloyardms.stashboxapi.shared.exception.types.DefaultGroupDeletionNotAllowedException;
import com.sloyardms.stashboxapi.shared.exception.types.DuplicateResourceException;
import com.sloyardms.stashboxapi.shared.exception.types.EmptyPatchBodyException;
import com.sloyardms.stashboxapi.shared.exception.types.InvalidPatchFieldException;
import com.sloyardms.stashboxapi.shared.exception.types.InvalidPatchStructureException;
import com.sloyardms.stashboxapi.shared.exception.types.InvalidSortFieldException;
import com.sloyardms.stashboxapi.shared.exception.types.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ProblemDetailFactory problemDetailFactory;

    // -------------------------------------------------------------------------
    // Server Errors
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex, HttpServletRequest request) {
        ProblemDetail problemDetail = problemDetailFactory.create(ErrorCatalog.INTERNAL_ERROR, request);
        String traceId = problemDetailFactory.getTraceId(problemDetail);
        log.error("[{}] Unexpected error, Path: {}", traceId, request.getRequestURI(), ex);
        return ResponseEntity.status(ErrorCatalog.INTERNAL_ERROR.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = problemDetailFactory.create(ErrorCatalog.DATA_INTEGRITY_VIOLATION, request);
        log.warn("[{}] Data integrity violation, Path: {}",
                problemDetailFactory.getTraceId(problemDetail), request.getRequestURI(), ex);
        return ResponseEntity.status(ErrorCatalog.DATA_INTEGRITY_VIOLATION.getStatus()).body(problemDetail);
    }

    // -------------------------------------------------------------------------
    // Auth
    // -------------------------------------------------------------------------

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(AccessDeniedException ex,
                                                                     HttpServletRequest request) {
        ProblemDetail problemDetail = problemDetailFactory.create(ErrorCatalog.FORBIDDEN, request);
        log.warn("[{}] Access denied for IP {}, Path: {}",
                problemDetailFactory.getTraceId(problemDetail),
                request.getRemoteAddr(),
                request.getRequestURI());
        return ResponseEntity.status(ErrorCatalog.FORBIDDEN.getStatus()).body(problemDetail);
    }

    // -------------------------------------------------------------------------
    // Endpoint
    // -------------------------------------------------------------------------

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoHandlerFoundException(NoHandlerFoundException ex,
                                                                       HttpServletRequest request) {
        Object[] details = {ex.getHttpMethod(), request.getRequestURI()};
        ProblemDetail problemDetail = problemDetailFactory.createWithArgs(ErrorCatalog.ENDPOINT_NOT_FOUND, details,
                request);
        log.warn("[{}] Endpoint not found, Path: {}",
                problemDetailFactory.getTraceId(problemDetail), request.getRequestURI());
        return ResponseEntity.status(ErrorCatalog.ENDPOINT_NOT_FOUND.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        Object[] details = {ex.getMethod()};
        ProblemDetail problemDetail = problemDetailFactory.createWithArgs(ErrorCatalog.HTTP_METHOD_NOT_ALLOWED,
                details, request);
        return ResponseEntity.status(ErrorCatalog.HTTP_METHOD_NOT_ALLOWED.getStatus()).body(problemDetail);
    }

    // -------------------------------------------------------------------------
    // Media Type
    // -------------------------------------------------------------------------

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        Object[] details = {ex.getSupportedMediaTypes()};
        ProblemDetail problemDetail = problemDetailFactory.createWithArgs(ErrorCatalog.MEDIA_TYPE_NOT_SUPPORTED,
                details, request);
        return ResponseEntity.status(ErrorCatalog.MEDIA_TYPE_NOT_SUPPORTED.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMediaTypeNotAcceptableException(
            HttpMediaTypeNotAcceptableException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = problemDetailFactory.create(ErrorCatalog.MEDIA_TYPE_NOT_ACCEPTABLE, request);
        return ResponseEntity.status(ErrorCatalog.MEDIA_TYPE_NOT_ACCEPTABLE.getStatus()).body(problemDetail);
    }

    // -------------------------------------------------------------------------
    // Request
    // -------------------------------------------------------------------------

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = problemDetailFactory.create(ErrorCatalog.MALFORMED_REQUEST_BODY, request);
        return ResponseEntity.status(ErrorCatalog.MALFORMED_REQUEST_BODY.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        Object[] details = {ex.getParameterName(), ex.getParameterType()};
        ProblemDetail problemDetail = problemDetailFactory.createWithArgs(ErrorCatalog.MISSING_REQUIRED_PARAMETER,
                details, request);
        return ResponseEntity.status(ErrorCatalog.MISSING_REQUIRED_PARAMETER.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ProblemDetail> handleMissingRequestHeaderException(
            MissingRequestHeaderException ex, HttpServletRequest request) {
        Object[] details = {ex.getHeaderName()};
        ProblemDetail problemDetail = problemDetailFactory.createWithArgs(ErrorCatalog.REQUEST_MISSING_HEADER,
                details, request);
        return ResponseEntity.status(ErrorCatalog.REQUEST_MISSING_HEADER.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String requiredTypeName = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        Object[] details = {ex.getName(), requiredTypeName};
        ProblemDetail problemDetail = problemDetailFactory.createWithArgs(ErrorCatalog.REQUEST_INVALID_PARAMETER_TYPE
                , details, request);
        return ResponseEntity.status(ErrorCatalog.REQUEST_INVALID_PARAMETER_TYPE.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(EmptyPatchBodyException.class)
    public ResponseEntity<ProblemDetail> handleEmptyPatchBodyException(
            EmptyPatchBodyException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = problemDetailFactory.create(ErrorCatalog.EMPTY_PATCH_BODY, request);
        return ResponseEntity.status(ErrorCatalog.EMPTY_PATCH_BODY.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(InvalidPatchFieldException.class)
    public ResponseEntity<ProblemDetail> handleInvalidPatchFieldException(
            InvalidPatchFieldException ex, HttpServletRequest request) {
        Object[] details = {ex.getFieldName()};
        ProblemDetail problemDetail = problemDetailFactory.createWithArgs(ErrorCatalog.INVALID_PATCH_FIELD_TYPE, details,
                request);
        return ResponseEntity.status(ErrorCatalog.INVALID_PATCH_FIELD_TYPE.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(InvalidPatchStructureException.class)
    public ResponseEntity<ProblemDetail> handleInvalidPatchStructureException(
            InvalidPatchStructureException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = problemDetailFactory.create(ErrorCatalog.NOT_A_JSON_OBJECT, request);
        return ResponseEntity.status(ErrorCatalog.NOT_A_JSON_OBJECT.getStatus()).body(problemDetail);
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ProblemDetail> handleBindException(BindException ex, HttpServletRequest request) {
        return buildValidationResponse(ex.getBindingResult(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        return buildValidationResponse(ex.getBindingResult(), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = problemDetailFactory.create(ErrorCatalog.VALIDATION_ERROR, request);

        List<FieldErrorDetail> fieldErrors = ex.getConstraintViolations().stream()
                .collect(Collectors.groupingBy(
                        v -> extractFieldName(v.getPropertyPath()),
                        Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList())
                ))
                .entrySet().stream()
                .map(e -> new FieldErrorDetail(e.getKey(), e.getValue()))
                .toList();

        if (!fieldErrors.isEmpty()) problemDetail.setProperty("fieldErrors", fieldErrors);

        return ResponseEntity.status(ErrorCatalog.VALIDATION_ERROR.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(InvalidSortFieldException.class)
    public ResponseEntity<ProblemDetail> handleInvalidSortFieldException(
            InvalidSortFieldException ex, HttpServletRequest request) {
        Object[] details = {ex.getInvalidField(), ex.getAllowedFields()};
        ProblemDetail problemDetail = problemDetailFactory.createWithArgs(ErrorCatalog.PAGEABLE_INVALID_SORT_FIELD,
                details, request);
        return ResponseEntity.status(ErrorCatalog.PAGEABLE_INVALID_SORT_FIELD.getStatus()).body(problemDetail);
    }

    // -------------------------------------------------------------------------
    // Resource
    // -------------------------------------------------------------------------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        Object[] details = {ex.getResourceType(), ex.getIdentifier(), ex.getValue()};
        ProblemDetail problemDetail = problemDetailFactory.createWithArgs(ErrorCatalog.RESOURCE_NOT_FOUND, details,
                request);
        problemDetail.setProperty("resourceType", ex.getResourceType());
        problemDetail.setProperty("identifier", ex.getIdentifier());
        problemDetail.setProperty("value", ex.getValue());
        return ResponseEntity.status(ErrorCatalog.RESOURCE_NOT_FOUND.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateResourceException(
            DuplicateResourceException ex, HttpServletRequest request) {
        Object[] details = {ex.getField(), ex.getValue()};
        ProblemDetail problemDetail = problemDetailFactory.createWithArgs(ErrorCatalog.DUPLICATE_RESOURCE, details,
                request);
        return ResponseEntity.status(ErrorCatalog.DUPLICATE_RESOURCE.getStatus()).body(problemDetail);
    }

    // -------------------------------------------------------------------------
    // Upload
    // -------------------------------------------------------------------------

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ProblemDetail> handleMultipartException(
            MultipartException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = problemDetailFactory.create(ErrorCatalog.MALFORMED_MULTIPART_REQUEST, request);
        return ResponseEntity.status(ErrorCatalog.MALFORMED_MULTIPART_REQUEST.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = problemDetailFactory.create(ErrorCatalog.UPLOAD_SIZE_EXCEEDED, request);
        return ResponseEntity.status(ErrorCatalog.UPLOAD_SIZE_EXCEEDED.getStatus()).body(problemDetail);
    }

    // -------------------------------------------------------------------------
    // ItemGroup
    // -------------------------------------------------------------------------

    @ExceptionHandler(DefaultGroupDeletionNotAllowedException.class)
    public ResponseEntity<ProblemDetail> handleDefaultGroupDeletionNotAllowedException(
            DefaultGroupDeletionNotAllowedException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = problemDetailFactory.create(ErrorCatalog.DEFAULT_GROUP_DELETION_NOT_ALLOWED,
                request);
        return ResponseEntity.status(ErrorCatalog.DEFAULT_GROUP_DELETION_NOT_ALLOWED.getStatus()).body(problemDetail);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ResponseEntity<ProblemDetail> buildValidationResponse(BindingResult bindingResult,
                                                                  HttpServletRequest request) {
        ProblemDetail problemDetail = problemDetailFactory.create(ErrorCatalog.VALIDATION_ERROR, request);

        List<FieldErrorDetail> fieldErrors = bindingResult.getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ))
                .entrySet().stream()
                .map(e -> new FieldErrorDetail(e.getKey(), e.getValue()))
                .toList();

        List<String> globalErrors = bindingResult.getGlobalErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .toList();

        if (!fieldErrors.isEmpty()) problemDetail.setProperty("fieldErrors", fieldErrors);
        if (!globalErrors.isEmpty()) problemDetail.setProperty("globalErrors", globalErrors);

        return ResponseEntity.status(ErrorCatalog.VALIDATION_ERROR.getStatus()).body(problemDetail);
    }

    private String extractFieldName(Path path) {
        String fieldName = null;
        for (Path.Node node : path) {
            fieldName = node.getName();
        }
        return fieldName != null ? fieldName : path.toString();
    }

}