package com.sloyardms.stashboxapi.controller.stashitem;

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
public class StashItemSearchIT  extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/item-groups/{groupSlug}/stash-items/search";

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return matching items")
        @Sql(scripts = {
                "/sql/data/users.sql",
                "/sql/data/item-groups.sql",
                "/sql/data/tags.sql",
                "/sql/data/stash-items.sql"
        })
        void shouldReturnMatchingItems() {
            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.DEV_RESOURCES_SLUG)
                    .queryParam("search", TestConstants.StashItemSearch.SPRING)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(1))
                    .body("page.totalElements", equalTo(1))
                    .body("content[0].title", equalTo("Spring Boot Documentation"));
        }

        @Test
        @DisplayName("Should support prefix search")
        @Sql(scripts = {
                "/sql/data/users.sql",
                "/sql/data/item-groups.sql",
                "/sql/data/tags.sql",
                "/sql/data/stash-items.sql"
        })
        void shouldSupportPrefixSearch() {
            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.DEV_RESOURCES_SLUG)
                    .queryParam("search", "post")
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(1))
                    .body("content[0].title", equalTo("PostgreSQL Docs"));
        }

        @Test
        @DisplayName("Should filter search results by single tag")
        @Sql(scripts = {
                "/sql/data/users.sql",
                "/sql/data/item-groups.sql",
                "/sql/data/tags.sql",
                "/sql/data/stash-items.sql"
        })
        void shouldFilterSearchResultsBySingleTag() {
            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.DEV_RESOURCES_SLUG)
                    .queryParam("search", TestConstants.StashItemSearch.SPRING)
                    .queryParam("tags", TestConstants.Tags.JAVA_SLUG)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(1))
                    .body("content[0].title", equalTo("Spring Boot Documentation"));
        }

        @Test
        @DisplayName("Should filter search results by multiple tags")
        @Sql(scripts = {
                "/sql/data/users.sql",
                "/sql/data/item-groups.sql",
                "/sql/data/tags.sql",
                "/sql/data/stash-items.sql"
        })
        void shouldFilterSearchResultsByMultipleTags() {
            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.DEV_RESOURCES_SLUG)
                    .queryParam("search", TestConstants.StashItemSearch.SPRING)
                    .queryParam("tags",
                            TestConstants.Tags.SPRING_BOOT_SLUG + "," +
                                    TestConstants.Tags.JAVA_SLUG)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(1))
                    .body("content[0].title", equalTo("Spring Boot Documentation"));
        }

        @Test
        @DisplayName("Should return empty page when there are no matches")
        @Sql(scripts = {
                "/sql/data/users.sql",
                "/sql/data/item-groups.sql",
                "/sql/data/tags.sql",
                "/sql/data/stash-items.sql"
        })
        void shouldReturnEmptyPageWhenThereAreNoMatches() {
            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.DEV_RESOURCES_SLUG)
                    .queryParam("search", "mongodb")
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(0))
                    .body("page.totalElements", equalTo(0));
        }

        @Test
        @DisplayName("Should sort by title ascending")
        @Sql(scripts = {
                "/sql/data/users.sql",
                "/sql/data/item-groups.sql",
                "/sql/data/tags.sql",
                "/sql/data/stash-items.sql"
        })
        void shouldSortByTitleAscending() {
            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.UNGROUPED_SLUG)
                    .queryParam("search", TestConstants.StashItemSearch.TESTING)
                    .queryParam("sort", "title,asc")
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.title",
                            contains(
                                    "Docker Compose Cheat Sheet",
                                    "Java Streams Guide",
                                    "Testing REST APIs"
                            ));
        }

        @Test
        @DisplayName("Should sort by relevance by default")
        @Sql(scripts = {
                "/sql/data/users.sql",
                "/sql/data/item-groups.sql",
                "/sql/data/tags.sql",
                "/sql/data/stash-items.sql"
        })
        void shouldSortByRelevanceByDefault() {
            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.UNGROUPED_SLUG)
                    .queryParam("search", TestConstants.StashItemSearch.TESTING)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(3))
                    .body("content.title", contains(
                            "Testing REST APIs",
                            "Docker Compose Cheat Sheet",
                            "Java Streams Guide"
                    ));
        }

        @Test
        @DisplayName("Should paginate search results")
        @Sql(scripts = {
                "/sql/data/users.sql",
                "/sql/data/item-groups.sql",
                "/sql/data/tags.sql",
                "/sql/data/stash-items.sql"
        })
        void shouldPaginateSearchResults() {
            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.UNGROUPED_SLUG)
                    .queryParam("search", TestConstants.StashItemSearch.TESTING)
                    .queryParam("page", 0)
                    .queryParam("size", 1)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(1))
                    .body("page.totalElements", equalTo(3))
                    .body("page.number", equalTo(0))
                    .body("page.size", equalTo(1));
        }
    }

    @Nested
    @DisplayName("General errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 400 when search parameter is missing")
        @Sql(scripts = {
                "/sql/data/users.sql",
                "/sql/data/item-groups.sql",
                "/sql/data/tags.sql",
                "/sql/data/stash-items.sql"
        })
        void shouldReturn400WhenSearchParameterIsMissing() {
            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.UNGROUPED_SLUG)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.MISSING_REQUIRED_PARAMETER.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.MISSING_REQUIRED_PARAMETER.getType().toString()));
        }

        @Test
        @DisplayName("Should return 400 when search parameter is blank")
        @Sql(scripts = {
                "/sql/data/users.sql",
                "/sql/data/item-groups.sql",
                "/sql/data/tags.sql",
                "/sql/data/stash-items.sql"
        })
        void shouldReturn400WhenSearchParameterIsBlank() {
            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.UNGROUPED_SLUG)
                    .queryParam("search", "   ")
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.VALIDATION_ERROR.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.VALIDATION_ERROR.getType().toString()));
        }

        @Test
        @DisplayName("Should return 422 when sort field is not allowed")
        @Sql(scripts = {
                "/sql/data/users.sql",
                "/sql/data/item-groups.sql",
                "/sql/data/tags.sql",
                "/sql/data/stash-items.sql"
        })
        void shouldReturn422WhenSortFieldIsNotAllowed() {
            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.UNGROUPED_SLUG)
                    .queryParam("search", TestConstants.StashItemSearch.JAVA)
                    .queryParam("sort", "invalid")
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(ErrorCatalog.PAGEABLE_INVALID_SORT_FIELD.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.PAGEABLE_INVALID_SORT_FIELD.getType().toString()));
        }

        @Test
        @DisplayName("Should return 400 when search parameter exceeds 100 characters")
        @Sql(scripts = {
                "/sql/data/users.sql",
                "/sql/data/item-groups.sql",
                "/sql/data/tags.sql",
                "/sql/data/stash-items.sql"
        })
        void shouldReturn400WhenSearchParameterExceeds100Characters() {
            String search = "a".repeat(101);

            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.UNGROUPED_SLUG)
                    .queryParam("search", search)
                    .when()
                    .get(ENDPOINT)
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
        @Sql(scripts = {
                "/sql/data/users.sql",
                "/sql/data/item-groups.sql",
                "/sql/data/tags.sql",
                "/sql/data/stash-items.sql"
        })
        void shouldReturn401WhenUserIsNotAuthenticated() {
            given()
                    .pathParam("groupSlug", TestConstants.Groups.UNGROUPED_SLUG)
                    .queryParam("search", TestConstants.StashItemSearch.JAVA)
                    .when()
                    .get(ENDPOINT + "/search")
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.UNAUTHORIZED.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }
    }
}
