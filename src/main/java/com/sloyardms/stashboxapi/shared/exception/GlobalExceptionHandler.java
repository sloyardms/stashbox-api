package com.sloyardms.stashboxapi.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ProblemDetailFactory problemDetailFactory;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex, HttpServletRequest request) {
        ProblemDetail problemDetail = problemDetailFactory.create(ErrorCatalog.INTERNAL_ERROR, request);
        String traceId = problemDetailFactory.getTraceId(problemDetail);
        log.error("[{}] Unexpected error, Path: {}", traceId, request.getRequestURI(), ex);
        return ResponseEntity.status(ErrorCatalog.INTERNAL_ERROR.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFoundException(ResourceNotFoundException ex,
                                                                         HttpServletRequest request) {
        Object[] details = {ex.getResourceType(), ex.getIdentifier(), ex.getValue()};
        ProblemDetail problemDetail = problemDetailFactory.createWithArgs(ErrorCatalog.RESOURCE_NOT_FOUND, details,
                request);
        String traceId = problemDetailFactory.getTraceId(problemDetail);

        problemDetail.setProperty("resourceType", ex.getResourceType());
        problemDetail.setProperty("identifier", ex.getIdentifier());
        problemDetail.setProperty("value", ex.getValue());

        log.warn("[{}] Resource not found, Path: {}, {} with {} = {},", traceId, request.getRequestURI(),
                ex.getResourceType(), ex.getIdentifier(), ex.getValue(), ex);
        return ResponseEntity.status(ErrorCatalog.RESOURCE_NOT_FOUND.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String parameterName = ex.getName();
        String requiredTypeName = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";

        Object[] details = {parameterName, requiredTypeName};
        ProblemDetail problemDetail = problemDetailFactory.createWithArgs(ErrorCatalog.REQUEST_INVALID_PARAMETER_TYPE
                , details, request);
        return ResponseEntity.status(ErrorCatalog.REQUEST_INVALID_PARAMETER_TYPE.getStatus()).body(problemDetail);
    }

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

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoHandlerFoundException(NoHandlerFoundException ex,
                                                                       HttpServletRequest request) {
        Object[] details = {ex.getHttpMethod(), request.getRequestURI()};
        ProblemDetail problemDetail = problemDetailFactory.createWithArgs(ErrorCatalog.ENDPOINT_NOT_FOUND, details,
                request);
        return ResponseEntity.status(ErrorCatalog.ENDPOINT_NOT_FOUND.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        Object[] details = {ex.getMethod()};
        ProblemDetail problemDetail = problemDetailFactory.createWithArgs(ErrorCatalog.HTTP_METHOD_NOT_ALLOWED,
                details, request);
        return ResponseEntity.status(ErrorCatalog.HTTP_METHOD_NOT_ALLOWED.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        ProblemDetail problemDetail = problemDetailFactory.create(ErrorCatalog.VALIDATION_ERROR, request);

        // Field-level errors: @NotBlank, @Email, @Size, etc.
        Map<String, List<String>> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));

        // Class-level errors: @AtLeastOneNonNullField, @ValidDateRange, etc.
        List<String> globalErrors = ex.getBindingResult()
                .getGlobalErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .toList();

        if (!fieldErrors.isEmpty()) problemDetail.setProperty("fieldErrors", fieldErrors);
        if (!globalErrors.isEmpty()) problemDetail.setProperty("globalErrors", globalErrors);

        return ResponseEntity.status(ErrorCatalog.VALIDATION_ERROR.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = problemDetailFactory.create(ErrorCatalog.MALFORMED_REQUEST_BODY, request);
        return ResponseEntity.status(ErrorCatalog.MALFORMED_REQUEST_BODY.getStatus()).body(problemDetail);
    }

}
