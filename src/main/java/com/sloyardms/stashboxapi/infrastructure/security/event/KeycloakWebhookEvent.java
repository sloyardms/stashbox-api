package com.sloyardms.stashboxapi.infrastructure.security.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakWebhookEvent(
        String id,
        String type,
        String realmId,
        String resourcePath, // present on admin events (e.g. "users/{id}")
        Long time,
        Map<String, String> details
) {

    public String userId() {
        return details != null ? details.get("userId") : null;
    }

}
