package com.sloyardms.stashboxapi.controller.user;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import com.sloyardms.stashboxapi.domain.user.dto.UserSettingsResponse;
import com.sloyardms.stashboxapi.shared.exception.ErrorCatalog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles("test")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class UserSettingsUpdateIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/users/me/settings";

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should update settings and return 200")
        @Sql({"/sql/data/users.sql"})
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
        @DisplayName("Should return 422 when darkModeEnabled is null")
        @Sql({"/sql/data/users.sql"})
        void shouldReturn422WhenDarkModeEnabledIsNull() {
            String request = """
                    {
                        "darkModeEnabled": null
                    }
                    """;

            givenNormalUserRequest()
                    .body(request)
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.VALIDATION_ERROR.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.VALIDATION_ERROR.getType().toString()));
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
