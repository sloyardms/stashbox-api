package com.sloyardms.stashboxapi.infrastructure.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Extra JWT checks layered on top of the default signature / issuer / timestamp validation.
 *
 * <p>The Keycloak realm is currently dedicated to Stashbox, so {@code accepted-authorized-parties}
 * pins tokens to the frontend client. If the realm is ever shared with other applications, keep
 * this list tight and (ideally) add a Keycloak audience mapper and populate
 * {@code accepted-audiences} as well.</p>
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtValidationProperties {

    /**
     * Values accepted in the token {@code aud} claim. A token passes if any of its audiences is
     * listed here. Empty = the {@code aud} claim is not checked.
     */
    private List<String> acceptedAudiences = new ArrayList<>();

    /**
     * Values accepted in the token {@code azp} (authorized party) claim — the Keycloak client the
     * token was issued to. Empty = the {@code azp} claim is not checked.
     */
    private List<String> acceptedAuthorizedParties = new ArrayList<>();

    /**
     * Reject tokens whose {@code typ} claim is present and not {@code Bearer} (e.g. ID tokens,
     * refresh tokens). Safe to leave enabled for Keycloak.
     */
    private boolean requireBearerTokenType = true;

    public boolean audienceOrPartyChecked() {
        return !acceptedAudiences.isEmpty() || !acceptedAuthorizedParties.isEmpty();
    }

}
