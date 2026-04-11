package com.sloyardms.stashboxapi.controller.user;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import com.sloyardms.stashboxapi.infrastructure.security.client.KeycloakClient;
import com.sloyardms.stashboxapi.shared.exception.ErrorCatalog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class UserSelfDeletionIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/users/me";

    @MockitoBean
    private KeycloakClient keycloakClient;

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should delete user profile and return 204")
        @Sql({"/sql/data/users.sql"})
        void shouldDeleteUserProfileAndReturn204() {
            givenNormalUserRequest()
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
                    .statusCode(ErrorCatalog.UNAUTHORIZED.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

    }

}
