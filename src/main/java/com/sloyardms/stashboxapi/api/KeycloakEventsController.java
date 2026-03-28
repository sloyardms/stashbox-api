package com.sloyardms.stashboxapi.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sloyardms.stashboxapi.domain.user.event.UserDeletedEvent;
import com.sloyardms.stashboxapi.infrastructure.security.event.KeycloakWebhookEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/webhooks/keycloak")
public class KeycloakEventsController {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${app.security.keycloak.webhook.hmac-secret}")
    private String webhookHmacSecret;

    private final ObjectMapper objectMapper;

    @PostMapping()
    public ResponseEntity<Void> handleEvent(
            @RequestHeader(value = "X-Keycloak-Signature", required = false) String signature,
            @RequestBody String rawBody) throws JsonProcessingException {

        if (signature == null || !isValidSignature(rawBody, signature)) {
            log.warn("Received webhook with invalid or missing signature - discarding silently");
            //Note: PhaseTwo webhook SPI keeps retrying if the response is not 200 or 201
            //send 200 back as fast as possible to prevent that
            return ResponseEntity.ok().build();
        }

        KeycloakWebhookEvent event = objectMapper.readValue(rawBody, KeycloakWebhookEvent.class);

        log.info("Received keycloak webhook event: {}", event.type());

        if (event.type().equals("admin.USER-DELETE")) {
            applicationEventPublisher.publishEvent(new UserDeletedEvent(event.userId()));
        } else {
            log.debug("Ignoring unhandled event type: {}", event.type());
        }

        return ResponseEntity.ok().build();
    }

    private boolean isValidSignature(String body, String receivedSignature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    webhookHmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }

            return MessageDigest.isEqual(
                    hexString.toString().getBytes(StandardCharsets.UTF_8),
                    receivedSignature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            System.out.println("Failed to compute HMAC signature");
            return false;
        }
    }

}