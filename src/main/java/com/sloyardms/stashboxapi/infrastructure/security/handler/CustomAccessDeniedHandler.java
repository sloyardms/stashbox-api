package com.sloyardms.stashboxapi.infrastructure.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sloyardms.stashboxapi.shared.exception.ErrorCatalog;
import com.sloyardms.stashboxapi.shared.exception.ProblemDetailFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ProblemDetailFactory problemDetailFactory;
    private final ObjectMapper objectMapper;

    @Override
    public void handle(@NonNull HttpServletRequest request, HttpServletResponse response,
                       @NonNull AccessDeniedException accessDeniedException) throws IOException {

        ProblemDetail problemDetail = problemDetailFactory.create(ErrorCatalog.FORBIDDEN, request);

        log.warn("Access denied: {} {} - traceId: {}", request.getMethod(), request.getRequestURI(),
                problemDetail.getProperties().get("traceId"));

        response.setStatus(problemDetail.getStatus());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), problemDetail);
    }

}
