package com.sloyardms.stashboxapi.infrastructure.security.config;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class KeycloakAdminConfig {

    @Value("${app.security.keycloak.server-url}")
    private String serverUrl;

    @Value("${app.security.keycloak.realm}")
    private String realm;

    @Value("${app.security.keycloak.client-id}")
    private String clientId;

    @Value("${app.security.keycloak.client-secret}")
    private String clientSecret;

    @Bean
    public Keycloak keycloakAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }

    @Bean
    public RealmResource stashboxRealm(Keycloak keycloak) {
        return keycloak.realm(realm);
    }

}
