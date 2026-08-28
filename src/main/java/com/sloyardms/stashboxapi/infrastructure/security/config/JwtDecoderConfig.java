package com.sloyardms.stashboxapi.infrastructure.security.config;

import com.sloyardms.stashboxapi.infrastructure.security.validation.KeycloakTokenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.StringUtils;

/**
 * Replaces Spring Boot's auto-configured resource-server {@link JwtDecoder} so that, on top of the
 * default signature / issuer / timestamp checks, every token is also run through
 * {@link KeycloakTokenValidator} (audience / authorized-party / token-type).
 *
 * <p>The JWKS endpoint uses the <em>internal</em> Keycloak URL while the issuer is the <em>public</em>
 * URL, so the decoder is built from {@code jwk-set-uri} when present and falls back to issuer
 * discovery (used by the integration tests, where only {@code issuer-uri} is set).</p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class JwtDecoderConfig {

    private final JwtValidationProperties jwtValidationProperties;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkSetUri;

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = StringUtils.hasText(jwkSetUri)
                ? NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build()
                : NimbusJwtDecoder.withIssuerLocation(issuerUri).build();

        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                new JwtIssuerValidator(issuerUri),
                new KeycloakTokenValidator(jwtValidationProperties)
        );
        decoder.setJwtValidator(validator);

        if (!jwtValidationProperties.audienceOrPartyChecked()) {
            log.warn("JWT audience/authorized-party validation is DISABLED "
                    + "(app.security.jwt.accepted-audiences and accepted-authorized-parties are both empty). "
                    + "Any token signed by the realm issuer is accepted. Configure at least one before "
                    + "sharing the Keycloak realm with other clients.");
        }

        return decoder;
    }

}
