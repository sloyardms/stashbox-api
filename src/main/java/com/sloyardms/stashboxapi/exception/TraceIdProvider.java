package com.sloyardms.stashboxapi.exception;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class TraceIdProvider {

    public static final String TRACE_ID_KEY = "traceId";

    public static String generate() {
        return "ERR-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

}
