package com.sloyardms.stashboxapi.controller.itemgroup;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import com.sloyardms.stashboxapi.domain.stash.repository.ItemGroupRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@ActiveProfiles("test")
@Sql(scripts = {"/sql/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class ItemGroupSearchIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/item-groups";
    @Autowired
    private ItemGroupRepository itemGroupRepository;

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return 200 and paginated list of item groups")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn200AndPaginatedListOfItemGroupsOfUser() {
            int numberOfItemGroups = 4; // matches normal user's item groups in item-groups.sql
            int expectedTotalPages = (int) Math.ceil((double) numberOfItemGroups / defaultPageSize);

            givenNormalUserRequest()
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(
                            "page.size", equalTo(defaultPageSize),
                            "page.totalElements", equalTo(numberOfItemGroups),
                            "page.totalPages", equalTo(expectedTotalPages),
                            "page.number", equalTo(0),
                            "content.size()", equalTo(numberOfItemGroups),
                            "content[0].id", notNullValue(),
                            "content[0].name", notNullValue(),
                            "content[0].slug", notNullValue(),
                            "content.position", contains(0, 1, 2, 3)
                    );
        }

        @Test
        @DisplayName("Should return 200 and paginated list of item groups sorted by name")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn200AndPaginatedListOfItemGroupsSortedByName() {
            givenNormalUserRequest()
                    .queryParam("sort", "name,asc")
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.name", contains("Design Inspiration", "Dev Resources", "Recipes", "Ungrouped"));

            givenNormalUserRequest()
                    .queryParam("sort", "name,desc")
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.name", contains("Ungrouped", "Recipes", "Dev Resources", "Design Inspiration"));
        }

    }

    @Nested
    @DisplayName("General Errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 400 when sort field is not allowed")
        void shouldReturn400WhenSortFieldIsNotAllowed() {
            givenNormalUserRequest()
                    .queryParam("sort", "not_allowed")
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.UNPROCESSABLE_CONTENT.value());
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
                    .get(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

}
