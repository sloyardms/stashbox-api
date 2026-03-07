package com.sloyardms.stashboxapi.config;

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

    // Contaniers
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER;

    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer<>(POSTGRES_IMAGE)
                .withReuse(true);

        POSTGRES_CONTAINER.start();
    }

    @DynamicPropertySource
    static void configurePostgresqlContainer(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
    }

}
