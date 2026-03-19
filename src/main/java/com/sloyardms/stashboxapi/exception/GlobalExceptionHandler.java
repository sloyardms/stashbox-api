package com.sloyardms.stashboxapi.exception;

import com.sloyardms.stashboxapi.common.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ProblemDetailFactory problemDetailFactory;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex, HttpServletRequest req) {
        ProblemDetail problemDetail = problemDetailFactory.create(ErrorCatalog.INTERNAL_ERROR, req);
        String traceId = getTraceId(problemDetail);
        log.error("[{}] Unexpected error, Path: {}", traceId, req.getRequestURI(), ex);
        return ResponseEntity.internalServerError().body(problemDetail);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFoundException(ResourceNotFoundException ex,
                                                                         HttpServletRequest req) {
        Object[] details = {ex.getResourceType(), ex.getIdentifier(), ex.getValue()};
        ProblemDetail problemDetail = problemDetailFactory.createWithArgs(ErrorCatalog.RESOURCE_NOT_FOUND, details,
                req);
        String traceId = getTraceId(problemDetail);

        problemDetail.setProperty("resourceType", ex.getResourceType());
        problemDetail.setProperty("identifier", ex.getIdentifier());
        problemDetail.setProperty("value", ex.getValue());

        log.warn("[{}] Resource not found, Path: {}, {} with {} = {},", traceId, req.getRequestURI(),
                ex.getResourceType(), ex.getIdentifier(), ex.getValue(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    private String getTraceId(ProblemDetail problemDetail) {
        Map<String, Object> props = problemDetail.getProperties();
        if (props == null) return "unknown";
        return (String) props.getOrDefault(TraceIdProvider.TRACE_ID_KEY, "unknown");
    }
}
