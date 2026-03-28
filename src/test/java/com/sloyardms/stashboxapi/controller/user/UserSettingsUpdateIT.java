package com.sloyardms.stashboxapi.controller.user;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import com.sloyardms.stashboxapi.domain.user.dto.UpdateUserSettingsRequest;
import com.sloyardms.stashboxapi.domain.user.dto.UserSettingsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

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
            UpdateUserSettingsRequest request = UpdateUserSettingsRequest.builder()
                    .darkMode(true).build();

            UserSettingsResponse response = normalUserRequest()
                    .body(request)
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.OK.value())
                    .extract().as(UserSettingsResponse.class);

            assertThat(response).isNotNull();
            assertThat(response.getDarkMode()).isTrue();
            assertThat(response.getUseFilters()).isFalse();
        }

    }

    @Nested
    @DisplayName("General Errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 400 when body is missing")
        void shouldReturn400WhenBodyIsMissing() {
            // Empty request
            UpdateUserSettingsRequest request = UpdateUserSettingsRequest.builder().build();

            normalUserRequest()
                    .body(request)
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("Should return 400 when fields are invalid")
        void shouldReturn400WhenFieldsAreInvalid() {
            String requestBody = """
                    {
                      "darkMode": 23,
                      "useFilters": "test"
                    }
                    """;
            normalUserRequest()
                    .body(requestBody)
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
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
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

}
