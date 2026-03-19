package com.sloyardms.stashboxapi.infrastructure.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sloyardms.stashboxapi.shared.exception.ErrorCatalog;
import com.sloyardms.stashboxapi.shared.exception.ProblemDetailFactory;
import com.sloyardms.stashboxapi.shared.exception.TraceIdProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ProblemDetailFactory problemDetailFactory;
    private final ObjectMapper objectMapper;

    @Override
    public void commence(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                         @NonNull AuthenticationException authException) throws IOException {

        ProblemDetail problemDetail = problemDetailFactory.create(ErrorCatalog.UNAUTHORIZED, request);

        log.warn("[{}] Unauthorized access attempt from IP {}, Path: {}",
                problemDetailFactory.getTraceId(problemDetail),
                request.getRemoteAddr(),
                request.getRequestURI());

        response.setStatus(problemDetail.getStatus());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), problemDetail);
    }

}
