package com.sloyardms.stashboxapi.controller.rules;

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
import static org.hamcrest.Matchers.*;

@ActiveProfiles("test")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class UrlRuleSearchIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/url-rules/search";

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return a page of url rules")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/url-rules.sql"})
        void shouldReturnAPaginatedListOfUrlRules() {
            int numberOfUrlRules = TestConstants.UrlRules.NORMAL_USER_URLRULE_COUNT;
            int expectedTotalPages = (int) Math.ceil((double) numberOfUrlRules / defaultPageSize);

            givenNormalUserRequest()
                    .queryParam("sort", "name,asc")
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(
                            "page.size", equalTo(defaultPageSize),
                            "page.totalElements", equalTo(numberOfUrlRules),
                            "page.totalPages", equalTo(expectedTotalPages),
                            "page.number", equalTo(0),
                            "content.size()", equalTo(numberOfUrlRules),
                            "content[0].id", equalTo(TestConstants.UrlRules.ALL_RECIPES_TITLE_ID.toString()),
                            "content[0].name", equalTo("AllRecipes Title"),
                            "content[0].domain", notNullValue(),
                            "content[0].active", notNullValue(),
                            "content[0].priority", notNullValue(),
                            "content[0].lastMatchedAt", notNullValue(),
                            "content[0].createdAt", notNullValue(),
                            "content[0].updatedAt", notNullValue()
                    );
        }

        @Test
        @DisplayName("Should return a page of url rules that match the search param")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/url-rules.sql"})
        void shouldReturnAPaginatedListOfUrlRulesBySearchParam() {
            int numberOfUrlRules = 1;
            int expectedTotalPages = (int) Math.ceil((double) numberOfUrlRules / defaultPageSize);

            givenNormalUserRequest()
                    .queryParam("q", "github")
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(
                            "page.size", equalTo(defaultPageSize),
                            "page.totalElements", equalTo(numberOfUrlRules),
                            "page.totalPages", equalTo(expectedTotalPages),
                            "page.number", equalTo(0),
                            "content.size()", equalTo(numberOfUrlRules),
                            "content[0].name", containsStringIgnoringCase("github")
                    );
        }

        @Test
        @DisplayName("Should return an empty page of url rules when searching other users data")
        void shouldReturnAnEmptyPageOfUrlRulesWhenSearchingOtherUsersData() {
            //normal user searching for admin user url rule
            givenNormalUserRequest()
                    .queryParam("q", "admin")
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("page.totalElements", equalTo(0));
        }

    }

    @Nested
    @DisplayName("General Errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 422 when sort field is not allowed")
        void shouldReturn422WhenSortFieldIsNotAllowed() {
            givenNormalUserRequest()
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
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.UNAUTHORIZED.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

    }

}
