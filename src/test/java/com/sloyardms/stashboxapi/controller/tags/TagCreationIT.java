package com.sloyardms.stashboxapi.controller.tags;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import com.sloyardms.stashboxapi.config.TestConstants;
import com.sloyardms.stashboxapi.domain.tag.dto.request.CreateTagRequest;
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
public class TagCreationIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/item-groups/{groupId}/tags";

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return 201 and the saved tag")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn201AndTheSavedTag() {
            CreateTagRequest body = new CreateTagRequest();
            body.setName("test tag");

            UUID groupId = TestConstants.Groups.DESIGN_ID;

            TagDetailResponse savedTag = givenNormalUserRequest()
                    .pathParam("groupId", groupId)
                    .body(body)
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .extract().as(TagDetailResponse.class);

            assertThat(savedTag).isNotNull();
            assertThat(savedTag.getId()).isNotNull();
            assertThat(savedTag.getName()).isEqualTo("test tag");
            assertThat(savedTag.getSlug()).isEqualTo("test-tag");
            assertThat(savedTag.getCreatedAt()).isNotNull();
            assertThat(savedTag.getUpdatedAt()).isNotNull();
            assertThat(savedTag.getItemCount()).isEqualTo(0);
            assertThat(savedTag.getLastUsed()).isNull();
        }

    }

    @Nested
    @DisplayName("General Errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 400 when body is missing")
        void shouldReturn400WhenBodyIsMissing() {
            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DESIGN_ID)
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.MALFORMED_REQUEST_BODY.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.MALFORMED_REQUEST_BODY.getType().toString()));
        }

        @Test
        @DisplayName("Should return 400 when name is blank")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn400WhenNameIsBlank() {
            CreateTagRequest body = new CreateTagRequest();

            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DESIGN_ID)
                    .body(body)
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.VALIDATION_ERROR.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.VALIDATION_ERROR.getType().toString()));
        }

        @Test
        @DisplayName("Should return 400 when name exceeds max length")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn400WhenNameExceedsMaxLength() {
            CreateTagRequest body = new CreateTagRequest();
            body.setName("t".repeat(100));

            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DESIGN_ID)
                    .when()
                    .body(body)
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.VALIDATION_ERROR.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.VALIDATION_ERROR.getType().toString()));
        }

        @Test
        @DisplayName("Should return 409 when a tag with the same name already exist")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql"})
        void shouldReturn409WhenATagWithTheSameNameAlreadyExist() {
            CreateTagRequest body = new CreateTagRequest();
            body.setName("UI");

            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DESIGN_ID)
                    .body(body)
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.DUPLICATE_RESOURCE.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.DUPLICATE_RESOURCE.getType().toString()));
        }

        @Test
        @DisplayName("Should return 404 when group does not exist")
        @Sql({"/sql/data/users.sql"})
        void shouldReturn404WhenGroupDoesNotExist() {
            CreateTagRequest body = new CreateTagRequest();
            body.setName("test tag");

            givenNormalUserRequest()
                    .pathParam("groupId", UUID.randomUUID())
                    .body(body)
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.RESOURCE_NOT_FOUND.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.RESOURCE_NOT_FOUND.getType().toString()));
        }

        @Test
        @DisplayName("Should return 409 when tag name is duplicate regardless of case")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql"})
        void shouldReturn409WhenTagNameIsDuplicateCaseInsensitively() {
            CreateTagRequest body = new CreateTagRequest();
            body.setName("ui"); // "UI" exists in tags.sql, slug would be "ui" — same slug

            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DESIGN_ID)
                    .body(body)
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.DUPLICATE_RESOURCE.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.DUPLICATE_RESOURCE.getType().toString()));
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
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.UNAUTHORIZED.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

        @Test
        @DisplayName("Should return 404 when group belongs to another user")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn404WhenGroupBelongsToAnotherUser() {
            CreateTagRequest body = new CreateTagRequest();
            body.setName("test tag");

            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.ADMIN_UNGROUPED_ID)
                    .body(body)
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.RESOURCE_NOT_FOUND.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.RESOURCE_NOT_FOUND.getType().toString()));
        }

    }

}