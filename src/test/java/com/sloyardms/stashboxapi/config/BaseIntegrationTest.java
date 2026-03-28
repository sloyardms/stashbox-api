package com.sloyardms.stashboxapi.config;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;

/**
 * Abstract base class for integration tests.
 * Provides RestAssured configuration, authentication methods, test user credentials and pagination helpers.
 * Extend this class to test application endpoints.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class BaseIntegrationTest extends TestContainersConfig {

    private static final String KEYCLOAK_TOKEN_PATH = "/realms/" + KEYCLOAK_REALM + "/protocol/openid-connect/token";
    private final String USER_ENDPOINT = "/api/v1/users/me";

    @LocalServerPort
    private int port;

    @Value("${spring.data.web.pageable.default-page-size}")
    public int defaultPageSize;
    public int smallPageSize;
    public int largePageSize;

    @Value("${tests.users.normal.username}")
    private String normalUserUsername;

    @Value("${tests.users.normal.password}")
    private String normalUserPassword;

    @Value("${tests.users.admin.username}")
    private String adminUserUsername;

    @Value("${tests.users.admin.password}")
    private String adminUserPassword;

    private String normalUserToken;
    private String adminUserToken;

    @BeforeEach
    void setupTestBase() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        Assert.isTrue(defaultPageSize > 5,
                "defaultPageSize must be greater than 5, current value is " + defaultPageSize);
        smallPageSize = defaultPageSize - 5;
        largePageSize = defaultPageSize;

        normalUserToken = generateAccessToken(normalUserUsername, normalUserPassword);
        adminUserToken = generateAccessToken(adminUserUsername, adminUserPassword);
    }

    public RequestSpecification normalUserRequest() {
        return authenticatedRequest(normalUserToken);
    }

    public RequestSpecification adminUserRequest() {
        return authenticatedRequest(adminUserToken);
    }

    private RequestSpecification authenticatedRequest(String token) {
        return given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    private String generateAccessToken(String username, String password) {
        return given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", KEYCLOAK_GRANT_TYPE)
                .formParam("client_id", KEYCLOAK_CLIENT_ID)
                .formParam("username", username)
                .formParam("password", password)
                .when()
                .post(KEYCLOAK_CONTAINER.getAuthServerUrl() + KEYCLOAK_TOKEN_PATH)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("access_token");
    }

    public void ensureNormalUserExists() {
        normalUserRequest()
                .when()
                .post(USER_ENDPOINT)
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    public void ensueAdminUserExists() {
        adminUserRequest()
                .when()
                .post(USER_ENDPOINT)
                .then()
                .statusCode(HttpStatus.OK.value());
    }

}
