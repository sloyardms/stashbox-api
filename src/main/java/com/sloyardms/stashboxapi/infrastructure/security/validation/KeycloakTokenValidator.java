package com.sloyardms.stashboxapi.infrastructure.security.validation;

import com.sloyardms.stashboxapi.infrastructure.security.config.JwtValidationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Validates that an incoming access token was actually issued for this API, closing the
 * token/client-confusion gap left by the default resource-server validation (which only checks
 * signature, issuer and timestamps).
 *
 * <ul>
 *   <li>{@code typ} must be {@code Bearer} when present — rejects ID tokens and refresh tokens.</li>
 *   <li>When configured, {@code azp} must be an accepted authorized party <em>or</em> {@code aud}
 *       must contain an accepted audience.</li>
 * </ul>
 */
@Slf4j
public class KeycloakTokenValidator implements OAuth2TokenValidator<Jwt> {

    private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6750#section-3.1";
    private static final String AZP_CLAIM = "azp";
    private static final String TYP_CLAIM = "typ";
    private static final String BEARER_TOKEN_TYPE = "Bearer";

    private final Set<String> acceptedAudiences;
    private final Set<String> acceptedAuthorizedParties;
    private final boolean requireBearerTokenType;

    public KeycloakTokenValidator(JwtValidationProperties properties) {
        this.acceptedAudiences = Set.copyOf(properties.getAcceptedAudiences());
        this.acceptedAuthorizedParties = Set.copyOf(properties.getAcceptedAuthorizedParties());
        this.requireBearerTokenType = properties.isRequireBearerTokenType();
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        List<OAuth2Error> errors = new ArrayList<>();

        if (requireBearerTokenType) {
            String tokenType = jwt.getClaimAsString(TYP_CLAIM);
            if (tokenType != null && !BEARER_TOKEN_TYPE.equalsIgnoreCase(tokenType)) {
                errors.add(error("Token type '" + tokenType + "' is not accepted by this resource server"));
            }
        }

        boolean checkParty = !acceptedAuthorizedParties.isEmpty();
        boolean checkAudience = !acceptedAudiences.isEmpty();

        if (checkParty || checkAudience) {
            String authorizedParty = jwt.getClaimAsString(AZP_CLAIM);
            List<String> audiences = jwt.getAudience() != null ? jwt.getAudience() : List.of();

            boolean partyMatches = checkParty && authorizedParty != null
                    && acceptedAuthorizedParties.contains(authorizedParty);
            boolean audienceMatches = checkAudience
                    && audiences.stream().anyMatch(acceptedAudiences::contains);

            if (!partyMatches && !audienceMatches) {
                errors.add(error("The token was not issued for this resource server "
                        + "(azp=" + authorizedParty + ", aud=" + audiences + ")"));
            }
        }

        return errors.isEmpty()
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(errors);
    }

    private static OAuth2Error error(String description) {
        return new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, description, ERROR_URI);
    }

}
