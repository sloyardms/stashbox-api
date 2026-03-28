package com.sloyardms.stashboxapi.infrastructure.security.converter;

import com.sloyardms.stashboxapi.infrastructure.security.dto.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
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
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String USERNAME_CLAIM = "preferred_username";
    private static final String EMAIL_CLAIM = "email";
    private static final String ROLES_CLAIM = "client_roles";

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        UUID userId = UUID.fromString(jwt.getSubject());
        String username = jwt.getClaimAsString(USERNAME_CLAIM);
        String email = jwt.getClaimAsString(EMAIL_CLAIM);
        List<String> roles = extractRoles(jwt);

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                userId,
                username,
                email,
                roles
        );

        return new UsernamePasswordAuthenticationToken(
                authenticatedUser,
                null,
                authorities
        );
    }

    private List<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM);

        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        return roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .toList();
    }

    private List<String> extractRoles(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM);
        return roles != null ? roles : Collections.emptyList();
    }

}
