package com.sloyardms.stashboxapi.shared.exception;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProblemDetailFactory {

    private final MessageSource messageSource;

    public ProblemDetail create(ErrorCatalog error, HttpServletRequest request) {
        return createWithArgs(error, null, request);
    }

    public ProblemDetail createWithDetail(ErrorCatalog error,
                                          String detail,
                                          HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        String title = resolveMessage(error.getTitleKey(), null, locale);

        return buildProblemDetail(error, title, detail, request);
    }

    public ProblemDetail createWithArgs(ErrorCatalog error,
                                        @Nullable Object[] detailArgs,
                                        HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        String title = resolveMessage(error.getTitleKey(), null, locale);
        String detail = resolveMessage(error.getDetailKey(), detailArgs, locale);

        return buildProblemDetail(error, title, detail, request);
    }

    private String resolveMessage(String key, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (NoSuchMessageException e) {
            log.error("Missing message key: {}", key);
            return key;
        }
    }

    private ProblemDetail buildProblemDetail(ErrorCatalog error, String title, String detail,
                                             HttpServletRequest request) {
        String traceId = TraceIdProvider.generate();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(error.getStatus(), detail);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setTitle(title);
        problem.setType(error.getType());
        problem.setProperty("timestamp", Instant.now().toString());
        problem.setProperty(TraceIdProvider.TRACE_ID_KEY, traceId);
        return problem;
    }

    public String getTraceId(ProblemDetail problemDetail) {
        Map<String, Object> props = problemDetail.getProperties();
        if (props == null) return "unknown";
        return (String) props.getOrDefault(TraceIdProvider.TRACE_ID_KEY, "unknown");
    }
}
