package com.sloyardms.stashboxapi.infrastructure.security.converter;

import com.sloyardms.stashboxapi.domain.user.service.UserService;
import com.sloyardms.stashboxapi.infrastructure.cache.UserIdCacheStore;
import com.sloyardms.stashboxapi.infrastructure.security.dto.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.Cache;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String USERNAME_CLAIM = "preferred_username";
    private static final String EMAIL_CLAIM = "email";
    private static final String ROLES_CLAIM = "client_roles";
    private final UserIdCacheStore userIdCacheStore;
    private final UserService userService;

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        List<String> roles = extractRoles(jwt);
        Collection<GrantedAuthority> authorities = toAuthorities(roles);

        UUID externalId = UUID.fromString(jwt.getSubject());
        UUID internalId;

        try {
            internalId = userIdCacheStore.getOrLoad(externalId, () -> userService.resolveInternalId(externalId));
        } catch (Cache.ValueRetrievalException e) {
            log.error("Failed to resolve internal user id for external id: {}", externalId, e.getCause());
            throw new AuthenticationServiceException("Unable to resolve user identity", e.getCause());
        }

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                internalId,
                externalId,
                jwt.getClaimAsString(USERNAME_CLAIM),
                jwt.getClaimAsString(EMAIL_CLAIM),
                roles
        );

        return new UsernamePasswordAuthenticationToken(authenticatedUser, null, authorities);
    }

    private Collection<GrantedAuthority> toAuthorities(List<String> roles) {
        return roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .toList();
    }

    private List<String> extractRoles(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM);
        return roles != null ? roles : Collections.emptyList();
    }

}
