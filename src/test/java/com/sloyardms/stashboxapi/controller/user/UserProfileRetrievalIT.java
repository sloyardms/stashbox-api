package com.sloyardms.stashboxapi.controller.user;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import com.sloyardms.stashboxapi.domain.user.dto.UserProfileResponse;
import com.sloyardms.stashboxapi.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
public class UserProfileRetrievalIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/users/me";

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return the user profile and return 200")
        void shouldReturnUserProfile() {
            userRepository.deleteAll();

            UserProfileResponse body = normalUserRequest()
                    .given()
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.OK.value())
                    .extract().as(UserProfileResponse.class);

            assertThat(body.getId()).isNotNull();
            assertThat(body.getSettings()).isNotNull();
            assertThat(body.getSettings().getDarkMode()).isFalse();
            assertThat(body.getSettings().getUseFilters()).isFalse();
            assertThat(body.getCreatedAt()).isNotNull();
            assertThat(body.getUpdatedAt()).isNotNull();
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
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

}
