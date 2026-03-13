package com.sloyardms.stashboxapi.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Component that extracts authenticated user information from the SecurityContext.
 * Provides access to the current user's details derived from the Keycloak JWT token.
 */
@Component
public class AuthenticatedUserProvider {

    private static final String USERNAME_CLAIM = "preferred_username";
    private static final String EMAIL_CLAIM = "email";

    /**
     * Returns the authenticated user extracted from the JWT stored in the security context
     *
     * @return the authenticated user information
     * @throws AuthenticationCredentialsNotFoundException if no authenticated user is present
     * @throws IllegalStateException                      if the principal type or required claims are invalid
     */
    public AuthenticatedUser getAuthenticatedUser() {
        Authentication auth = getAuthentication();
        Object principal = auth.getPrincipal();

        if (auth.getPrincipal() instanceof Jwt jwt) {

            UUID providerId = parseSubject(requireClaim(jwt, "sub"));
            String username = requireClaim(jwt, USERNAME_CLAIM);
            String email = requireClaim(jwt, EMAIL_CLAIM);

            return new AuthenticatedUser(providerId, username, email);
        }
        throw new IllegalStateException(
                "Unsupported principal type: " + auth.getPrincipal().getClass().getName()
        );
    }

    /**
     * Retrieves the current Authentication from the security context
     *
     * @return the current authentication
     * @throws AuthenticationCredentialsNotFoundException if the security context does not contain
     *                                                    *         an authenticated principal
     */
    private Authentication getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("No authenticated user found in security context");
        }

        return auth;
    }

    /**
     * Retrieves and validates a required claim from the JWT
     *
     * @param jwt   the JWT token
     * @param claim the name of the claim
     * @return the claim value
     * @throws IllegalStateException if the claim is missing or empty
     */
    private String requireClaim(Jwt jwt, String claim) {
        String value = jwt.getClaimAsString(claim);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(claim + " claim is missing or empty");
        }

        return value;
    }

    /**
     * Parses the JWT subject into a UUID
     *
     * @param value the subject claim value
     * @return the parsed UUID
     * @throws IllegalStateException if the subject is not a valid UUID
     */
    private UUID parseSubject(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "JWT subject is not a valid UUID: " + value, e
            );
        }
    }
}
