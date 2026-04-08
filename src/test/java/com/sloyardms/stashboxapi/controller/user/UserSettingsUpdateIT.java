package com.sloyardms.stashboxapi.controller.user;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import com.sloyardms.stashboxapi.domain.user.dto.UserSettingsResponse;
import com.sloyardms.stashboxapi.shared.exception.ErrorCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles("test")
public class UserSettingsUpdateIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/users/me/settings";

    @BeforeEach
    public void setup() {
        ensureNormalUserExists();
    }

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should update settings and return 200")
        void shouldUpdateSettingsAndReturn200() {
            String request = """
                    {
                        "darkModeEnabled": true
                    }
                    """;

            UserSettingsResponse response = givenNormalUserRequest()
                    .body(request)
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract().as(UserSettingsResponse.class);

            assertThat(response).isNotNull();
            assertThat(response.getDarkModeEnabled()).isTrue();
            assertThat(response.getFiltersEnabled()).isFalse();
        }

    }

    @Nested
    @DisplayName("General Errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 400 when body is missing")
        void shouldReturn400WhenBodyIsMissing() {
            givenNormalUserRequest()
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.MALFORMED_REQUEST_BODY.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.MALFORMED_REQUEST_BODY.getType().toString()));
        }

        @Test
        @DisplayName("Should return 400 when body is empty")
        void shouldReturn400WhenBodyIsEmpty() {
            givenNormalUserRequest()
                    .body("{}")
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.EMPTY_PATCH_BODY.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.EMPTY_PATCH_BODY.getType().toString()));
        }

        @Test
        @DisplayName("Should return 400 when fields have invalid types")
        void shouldReturn400WhenFieldsAreInvalid() {
            String requestBody = """
                    {
                      "darkModeEnabled": 23,
                      "filtersEnabled": "test"
                    }
                    """;
            givenNormalUserRequest()
                    .body(requestBody)
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.INVALID_PATCH_FIELD_TYPE.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.INVALID_PATCH_FIELD_TYPE.getType().toString()));
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
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

    }

}
