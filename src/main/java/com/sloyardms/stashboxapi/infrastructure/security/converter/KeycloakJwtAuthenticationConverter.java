package com.sloyardms.stashboxapi.infrastructure.security.converter;

import com.sloyardms.stashboxapi.infrastructure.security.AuthenticatedUser;
import com.sloyardms.stashboxapi.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String USERNAME_CLAIM = "preferred_username";
    private static final String EMAIL_CLAIM = "email";
    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";

    private final UserService userService;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        UUID providerId = UUID.fromString(jwt.getSubject());
        String username = jwt.getClaimAsString(USERNAME_CLAIM);
        String email = jwt.getClaimAsString(EMAIL_CLAIM);

        UUID userId = userService.resolveOrCreateUser(providerId, username, email);

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                userId,
                providerId,
                username,
                email
        );

        return new UsernamePasswordAuthenticationToken(
                authenticatedUser,
                null,
                authorities
        );
    }

    private List<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaimAsMap(REALM_ACCESS_CLAIM);

        if (claims == null || claims.isEmpty()) {
            return Collections.emptyList();
        }

        Object rolesObj = claims.get(ROLES_CLAIM);
        if (!(rolesObj instanceof List<?>)) {
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) rolesObj;

        return roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .toList();
    }

}
