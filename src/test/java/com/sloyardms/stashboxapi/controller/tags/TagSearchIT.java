package com.sloyardms.stashboxapi.controller.tags;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import com.sloyardms.stashboxapi.config.TestConstants;
import com.sloyardms.stashboxapi.shared.exception.ErrorCatalog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@ActiveProfiles("test")
@Sql(scripts = {"/sql/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class TagSearchIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/item-groups/{groupId}/tags";

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return 200 and paginated list of tags")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql"})
        void shouldReturn200AndPaginatedListOfTags() {
            int numberOfTags = TestConstants.Tags.DESIGN_COUNT;
            int expectedTotalPages = (int) Math.ceil((double) numberOfTags / defaultPageSize);

            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DESIGN_ID)
                    .queryParam("sort", "name,asc")
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(
                            "page.size", equalTo(defaultPageSize),
                            "page.totalElements", equalTo(numberOfTags),
                            "page.totalPages", equalTo(expectedTotalPages),
                            "page.number", equalTo(0),
                            "content.size()", equalTo(numberOfTags),
                            "content[0].id", notNullValue(),
                            "content[0].name", equalTo("Color Palette"),
                            "content[0].slug", equalTo("color-palette"),
                            "content[0].itemCount", equalTo(0)
                    );
        }

        @Test
        @DisplayName("Should return 200 and paginated list of tags that match the search text")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql"})
        void shouldReturn200AndPaginatedListOfTagsWithSearchQuery() {
            int numberOfTags = 1;
            int expectedTotalPages = (int) Math.ceil((double) numberOfTags / defaultPageSize);

            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DESIGN_ID)
                    .queryParam("search", "grap")
                    .queryParam("sort", "name,asc")
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(
                            "page.size", equalTo(defaultPageSize),
                            "page.totalElements", equalTo(numberOfTags),
                            "page.totalPages", equalTo(expectedTotalPages),
                            "page.number", equalTo(0),
                            "content.size()", equalTo(numberOfTags),
                            "content[0].id", notNullValue(),
                            "content[0].name", equalTo("Typography"),
                            "content[0].slug", equalTo("typography"),
                            "content[0].itemCount", equalTo(0)
                    );
        }

    }

    @Nested
    @DisplayName("General Errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 422 when sort field is not allowed")
        void shouldReturn422WhenSortFieldIsNotAllowed() {
            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DESIGN_ID)
                    .queryParam("sort", "not_allowed")
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.PAGEABLE_INVALID_SORT_FIELD.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.PAGEABLE_INVALID_SORT_FIELD.getType().toString()));
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
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.UNAUTHORIZED.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

    }
}
