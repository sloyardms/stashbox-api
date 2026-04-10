package com.sloyardms.stashboxapi.controller.tags;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import com.sloyardms.stashboxapi.config.TestConstants;
import com.sloyardms.stashboxapi.domain.tag.dto.response.TagDetailResponse;
import com.sloyardms.stashboxapi.shared.exception.ErrorCatalog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles("test")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class TagRetrievalIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/item-groups/{groupId}/tags/{tagId}";

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return the tag detail and 200")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql"})
        void shouldReturnTheTagDetailAnd200() {
            TagDetailResponse response = givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.RECIPES_ID)
                    .pathParam("tagId", TestConstants.Tags.QUICK_MEALS_ID)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.OK.value())
                    .extract().body().as(TagDetailResponse.class);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(TestConstants.Tags.QUICK_MEALS_ID);
            assertThat(response.getName()).isEqualTo("Quick Meals");
            assertThat(response.getSlug()).isEqualTo("quick-meals");
            assertThat(response.getCreatedAt()).isNotNull();
            assertThat(response.getUpdatedAt()).isNotNull();
            assertThat(response.getItemCount()).isEqualTo(0);
            assertThat(response.getLastUsed()).isNull();
        }

    }

    @Nested
    @DisplayName("General Errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 404 when the tag does not exist")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn404WhenTheTagDoesNotExist() {
            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.RECIPES_ID)
                    .pathParam("tagId", UUID.randomUUID())
                    .when()
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
                    .pathParam("groupId", "invalidGroupId")
                    .pathParam("tagId", "invalidTagId")
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.REQUEST_INVALID_PARAMETER_TYPE.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.REQUEST_INVALID_PARAMETER_TYPE.getType().toString()));
        }

        @Test
        @DisplayName("Should return 404 when tag does not belong to the specified group")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql"})
        void shouldReturn404WhenTagDoesNotBelongToGroup() {
            // QUICK_MEALS_ID belongs to RECIPES group, not DEV_RESOURCES
            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DEV_RESOURCES_ID)
                    .pathParam("tagId", TestConstants.Tags.QUICK_MEALS_ID)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(ErrorCatalog.RESOURCE_NOT_FOUND.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.RESOURCE_NOT_FOUND.getType().toString()));
        }

    }

    @Nested
    @DisplayName("Authentication and Authorization Errors")
    class AuthenticationAndAuthorization {

        @Test
        @DisplayName("Should return 401 when the user is not authenticated")
        void shouldReturn401WhenUserIsNotAuthenticated() {
            given()
                    .pathParam("groupId", UUID.randomUUID())
                    .pathParam("tagId", UUID.randomUUID())
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

        @Test
        @DisplayName("Should return 404 when tag belongs to another user")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql"})
        void shouldReturn404WhenTheTagBelongsToAnotherUser() {
            //normal user fetching admin tag
            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.ADMIN_UNGROUPED_ID)
                    .pathParam("tagId", TestConstants.Tags.ADMIN_JAVA_ID)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.RESOURCE_NOT_FOUND.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.RESOURCE_NOT_FOUND.getType().toString()));
        }

    }

}
