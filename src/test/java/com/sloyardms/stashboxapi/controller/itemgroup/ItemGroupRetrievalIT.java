package com.sloyardms.stashboxapi.controller.itemgroup;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import com.sloyardms.stashboxapi.config.TestConstants;
import com.sloyardms.stashboxapi.domain.stash.dto.response.ItemGroupDetailResponse;
import com.sloyardms.stashboxapi.shared.exception.ErrorCatalog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles("test")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ItemGroupRetrievalIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/item-groups/{id}";

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return the item group detail and 200")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn200AndTheItemGroup() {
            ItemGroupDetailResponse response = givenNormalUserRequest()
                    .when()
                    .pathParam("id", TestConstants.USER_DEFAULT_GROUP_ID)
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract().as(ItemGroupDetailResponse.class);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isNotNull();
            assertThat(response.getName()).isNotNull().isEqualTo("Ungrouped");
            assertThat(response.getSlug()).isNotNull().isEqualTo("ungrouped");
            assertThat(response.getDescription()).isNull();
            assertThat(response.getIcon()).isNull();
            assertThat(response.getDefaultGroup()).isTrue();
            assertThat(response.getPosition()).isEqualTo(0);
            assertThat(response.getSettings()).isNotNull();
            assertThat(response.getSettings().isRequiredTitle()).isFalse();
            assertThat(response.getSettings().isUniqueTitle()).isFalse();
            assertThat(response.getSettings().isRequiredUrl()).isFalse();
            assertThat(response.getSettings().isUniqueUrl()).isFalse();
            assertThat(response.getSettings().isRequiredImage()).isFalse();
        }

    }

    @Nested
    @DisplayName("General Errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 404 when the item group does not exist")
        void shouldReturn404WhenItemGroupDoesNotExist() {
            UUID nonExistentItemGroupId = UUID.randomUUID();

            givenNormalUserRequest()
                    .when()
                    .pathParam("id", nonExistentItemGroupId)
                    .get(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.RESOURCE_NOT_FOUND.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.RESOURCE_NOT_FOUND.getType().toString()));
        }

        @Test
        @DisplayName("Should return 404 when item group belongs to another user")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn404WhenItemGroupBelongsToAnotherUser() {
            givenNormalUserRequest()
                    .when()
                    .pathParam("id", TestConstants.ADMIN_USER_DEFAULT_GROUP_ID)
                    .get(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.RESOURCE_NOT_FOUND.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.RESOURCE_NOT_FOUND.getType().toString()));
        }

        @Test
        @DisplayName("Should return 400 when id is not a valid UUID")
        void shouldReturn400WhenIdIsNotValidUUID() {
            givenNormalUserRequest()
                    .when()
                    .pathParam("id", "invalidUuid")
                    .get(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.REQUEST_INVALID_PARAMETER_TYPE.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.REQUEST_INVALID_PARAMETER_TYPE.getType().toString()));
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
                    .pathParam("id", UUID.randomUUID())
                    .get(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

    }

}
