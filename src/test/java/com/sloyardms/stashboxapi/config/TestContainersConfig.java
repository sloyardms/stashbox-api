package com.sloyardms.stashboxapi.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Abstract base class for integration tests requiring external services via Testcontainers.
 * Extend this class to get shared and reusable containers with Spring properties automatically
 * configured via {@link DynamicPropertySource}
 */
public abstract class TestContainersConfig {

    // Image names
    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:18.2");
    private static final DockerImageName KEYCLOAK_IMAGE = DockerImageName.parse("quay.io/keycloak/keycloak:26.5");

    protected static final String KEYCLOAK_REALM = "stashbox";
    protected static final String KEYCLOAK_CLIENT_ID = "stashbox-frontend";
    protected static final String KEYCLOAK_GRANT_TYPE = "password";

    // Containers
    protected static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>(POSTGRES_IMAGE);

    protected static final KeycloakContainer KEYCLOAK_CONTAINER =
            new KeycloakContainer(KEYCLOAK_IMAGE.toString())
                    .withRealmImportFile("config/keycloak/stashbox-realm-test.json");

    static {
        POSTGRES_CONTAINER.start();
        KEYCLOAK_CONTAINER.start();
    }

    @DynamicPropertySource
    static void configureContainers(DynamicPropertyRegistry registry) {
        // Postgresql
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        // Keycloak
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> KEYCLOAK_CONTAINER.getAuthServerUrl() + "/realms/" + KEYCLOAK_REALM);
        registry.add("spring.security.oauth2.client.provider.keycloak.issuer-uri",
                () -> KEYCLOAK_CONTAINER.getAuthServerUrl() + "/realms/" + KEYCLOAK_REALM);
        registry.add("app.security.keycloak.server-url", KEYCLOAK_CONTAINER::getAuthServerUrl);
    }

}
