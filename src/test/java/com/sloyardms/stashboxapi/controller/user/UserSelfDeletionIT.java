package com.sloyardms.stashboxapi.controller.user;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import com.sloyardms.stashboxapi.infrastructure.security.client.KeycloakClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
public class UserSelfDeletionIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/users/me";

    @MockitoBean
    private KeycloakClient keycloakClient;

    @BeforeEach
    public void setup() {
        ensureNormalUserExists();
    }

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should delete user profile and return 204")
        void shouldDeleteUserProfileAndReturn204() {
            normalUserRequest()
                    .when()
                    .delete(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            verify(keycloakClient).deleteUser(anyString());
        }

    }

    @Nested
    @DisplayName("Authentication and Authorization Errors")
    class AuthenticationAndAuthorization {

        @Test
        @DisplayName("Should return 401 when the user is not authenticated")
        void shouldReturn401WhenUserIsNotAuthenticated() {
            given()
                    .when()
                    .delete(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

}
