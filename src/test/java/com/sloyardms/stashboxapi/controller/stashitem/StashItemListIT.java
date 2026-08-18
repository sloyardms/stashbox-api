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

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@ActiveProfiles("test")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class StashItemListIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/item-groups/{groupSlug}/stash-items";

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should list all items in the requested group")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql", "/sql/data/stash-items.sql"})
        void shouldListAllItemsInTheRequestedGroup() {
            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.DEV_RESOURCES_SLUG)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .log().body()
                    .body("content.size()", equalTo(TestConstants.StashItems.DEV_RESOURCES_COUNT))
                    .body("page.totalElements", equalTo(TestConstants.StashItems.DEV_RESOURCES_COUNT))
                    .body("content.title",
                            containsInAnyOrder(
                                    "Spring Boot Documentation",
                                    "PostgreSQL Docs"
                            ));
        }

        @Test
        @DisplayName("Should filter items by single tag")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql", "/sql/data/stash-items.sql"})
        void shouldFilterItemsBySingleTag() {
            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.DEV_RESOURCES_SLUG)
                    .queryParam("tags", TestConstants.Tags.JAVA_SLUG)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(1))
                    .body("page.totalElements", equalTo(1))
                    .body("content[0].title", equalTo("Spring Boot Documentation"));
        }

        @Test
        @DisplayName("Should filter items by multiple tags using AND semantics")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql", "/sql/data/stash-items.sql"})
        void shouldFilterItemsByMultipleTags() {
            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.DEV_RESOURCES_SLUG)
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
        @DisplayName("Should return an empty page when no item matches the requested tag")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql", "/sql/data/stash-items.sql"})
        void shouldReturnEmptyPageWhenNoItemMatchesTheRequestedTag() {
            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.DEV_RESOURCES_SLUG)
                    .queryParam("tags", TestConstants.Tags.DOCKER_SLUG)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(0))
                    .body("page.totalElements", equalTo(0));
        }

        @Test
        @DisplayName("Should sort items by title ascending")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql", "/sql/data/stash-items.sql"})
        void shouldSortItemsByTitleAscending() {
            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.DEV_RESOURCES_SLUG)
                    .queryParam("sort", "title,asc")
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.title",
                            contains(
                                    "PostgreSQL Docs",
                                    "Spring Boot Documentation"
                            ));
        }

        @Test
        @DisplayName("Should return items ordered by createdAt descending by default")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql", "/sql/data/stash-items.sql"})
        void shouldSortItemsByCreatedAtDescending() {
            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.DEV_RESOURCES_SLUG)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.title",
                            contains(
                                    "PostgreSQL Docs",
                                    "Spring Boot Documentation"
                            ));
        }

        @Test
        @DisplayName("Should paginate results")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql", "/sql/data/stash-items.sql"})
        void shouldPaginateResults() {
            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.UNGROUPED_SLUG)
                    .queryParam("page", 0)
                    .queryParam("size", 2)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(2))
                    .body("page.totalElements", equalTo(TestConstants.StashItems.UNGROUPED_COUNT))
                    .body("page.number", equalTo(0))
                    .body("page.size", equalTo(2));
        }

    }

    @Nested
    @DisplayName("General errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 422 when sort field is not allowed")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql", "/sql/data/stash-items.sql"})
        void shouldReturn422WhenSortFieldIsNotAllowed() {
            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.UNGROUPED_SLUG)
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
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql", "/sql/data/stash-items.sql"})
        void shouldReturn401WhenUserIsNotAuthenticated() {
            given()
                    .pathParam("groupSlug", UUID.randomUUID())
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.UNAUTHORIZED.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

    }

}
