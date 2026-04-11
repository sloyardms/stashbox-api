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
public class TagUpdateIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/item-groups/{groupId}/tags/{tagId}";

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return 200 and update the tag")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql"})
        void shouldReturn200AndUpdateTheTag() {
            String request = """
                    {
                        "name": "new name"
                    }
                    """;

            TagDetailResponse response = givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DESIGN_ID)
                    .pathParam("tagId", TestConstants.Tags.UX_ID)
                    .body(request)
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract().as(TagDetailResponse.class);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(TestConstants.Tags.UX_ID);
            assertThat(response.getName()).isEqualTo("new name");
            assertThat(response.getSlug()).isEqualTo("new-name");
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
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql"})
        void shouldReturn404WhenTheTagDoesNotExist() {
            String request = """
                    {
                        "name": "new name"
                    }
                    """;

            // Doesn't distinguish between "tag not found", "group not found", or "belongs to another user"
            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DESIGN_ID)
                    .pathParam("tagId", UUID.randomUUID())
                    .body(request)
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.RESOURCE_NOT_FOUND.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.RESOURCE_NOT_FOUND.getType().toString()));
        }

        @Test
        @DisplayName("Should return 409 when tag name already exist")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql"})
        void shouldReturn409WhenTheTagNameAlreadyExists() {
            String request = """
                    {
                        "name": "UX"
                    }
                    """;

            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DESIGN_ID)
                    .pathParam("tagId", TestConstants.Tags.UI_ID)
                    .body(request)
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.DUPLICATE_RESOURCE.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.DUPLICATE_RESOURCE.getType().toString()));
        }

        @Test
        @DisplayName("Should return 422 when name is blank")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql"})
        void shouldReturn422WhenNameIsBlank() {
            String request = """
                    {
                        "name": ""
                    }
                    """;

            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DESIGN_ID)
                    .pathParam("tagId", TestConstants.Tags.UI_ID)
                    .body(request)
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.VALIDATION_ERROR.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.VALIDATION_ERROR.getType().toString()));
        }

        @Test
        @DisplayName("Should return 422 when name exceeds max length")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql"})
        void shouldReturn422WhenNameExceedsMaxLength() {
            String name = "N".repeat(100);
            String request = String.format("{ \"name\": \"%s\" }", name);

            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DESIGN_ID)
                    .pathParam("tagId", TestConstants.Tags.UI_ID)
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
                    .pathParam("groupId", TestConstants.Groups.DESIGN_ID)
                    .pathParam("tagId", TestConstants.Tags.UI_ID)
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.UNAUTHORIZED.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

    }

}
