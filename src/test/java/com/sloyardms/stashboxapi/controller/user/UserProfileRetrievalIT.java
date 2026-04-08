package com.sloyardms.stashboxapi.controller.user;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import com.sloyardms.stashboxapi.config.TestConstants;
import com.sloyardms.stashboxapi.domain.stash.model.ItemGroup;
import com.sloyardms.stashboxapi.domain.stash.repository.ItemGroupRepository;
import com.sloyardms.stashboxapi.domain.user.dto.UserProfileResponse;
import com.sloyardms.stashboxapi.domain.user.repository.UserRepository;
import com.sloyardms.stashboxapi.shared.exception.ErrorCatalog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles("test")
public class UserProfileRetrievalIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/users/me";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemGroupRepository itemGroupRepository;

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return the user profile and return 200")
        void shouldReturnUserProfile() {
            userRepository.deleteAll();

            UserProfileResponse body = givenNormalUserRequest()
                    .given()
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.OK.value())
                    .extract().as(UserProfileResponse.class);

            // Verify user profile
            assertThat(body.getId()).isNotNull();
            assertThat(body.getSettings()).isNotNull();
            assertThat(body.getSettings().getDarkModeEnabled()).isFalse();
            assertThat(body.getSettings().getFiltersEnabled()).isFalse();
            assertThat(body.getCreatedAt()).isNotNull();
            assertThat(body.getUpdatedAt()).isNotNull();

            // Verify that a default group was assigned to the user
            ItemGroup defaultGroup =
                    itemGroupRepository.findAllByUserId(TestConstants.USER_ID, Pageable.unpaged())
                            .getContent().getFirst();
            assertThat(defaultGroup.getName()).isEqualTo("Ungrouped");
            assertThat(defaultGroup.getSlug()).isEqualTo("ungrouped");
            assertThat(defaultGroup.isDefaultGroup()).isTrue();
            assertThat(defaultGroup.getPosition()).isEqualTo(0);
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
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

    }

}
