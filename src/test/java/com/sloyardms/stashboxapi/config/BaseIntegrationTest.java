package com.sloyardms.stashboxapi.config;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.util.Assert;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Abstract base class for integration tests.
 * Provides RestAssured configuration, authentication methods, test user credentials and pagination helpers.
 * Extend this class to test application endpoints.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class BaseIntegrationTest extends TestContainersConfig {

    @LocalServerPort
    private int port;

    @Value("${spring.data.web.pageable.default-page-size}")
    public int defaultPageSize;
    public int smallPageSize;
    public int largePageSize;

    @Value("${tests.users.normal.username}")
    public String normalUserUsername;

    @Value("${tests.users.normal.password}")
    public String normalUserPassword;

    @Value("${tests.users.admin.username}")
    public String adminUserUsername;

    @Value("${tests.users.admin.password}")
    public String adminUserPassword;

    @BeforeEach
    void setupTestBase() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        Assert.isTrue(defaultPageSize > 5,
                "defaultPageSize must be greater than 5, current value is " + defaultPageSize);
        smallPageSize = defaultPageSize - 5;
        largePageSize = defaultPageSize;
    }

}
