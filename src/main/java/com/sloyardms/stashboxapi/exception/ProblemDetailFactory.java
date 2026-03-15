package com.sloyardms.stashboxapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProblemDetailFactory {

    private final MessageSource messageSource;

    public ProblemDetail create(ErrorCatalog error, HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        String title = messageSource.getMessage(error.getTitle(), null, locale);
        String detail = messageSource.getMessage(error.getDetail(), null, locale);

        return buildProblemDetail(error, title, detail, request);
    }

    public ProblemDetail createWithDetail(ErrorCatalog error, String detail, HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        String title = messageSource.getMessage(error.getTitle(), null, locale);

        return buildProblemDetail(error, title, detail, request);
    }

    private ProblemDetail buildProblemDetail(ErrorCatalog error, String title, String detail,
                                             HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(error.getStatus(), detail);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setTitle(title);
        problem.setType(error.getType());
        problem.setProperty("timestamp", Instant.now().toString());
        problem.setProperty("traceId", generateTraceId());
        return problem;
    }

    private String generateTraceId() {
        return "ERR-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}
