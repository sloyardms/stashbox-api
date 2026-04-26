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

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@ActiveProfiles("test")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class UrlRuleDomainLookupIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/item-groups/{groupId}/url-rules";

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return a list of active url rules matching the domain an 200")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/url-rules.sql"})
        void shouldReturnListOfUrlRules() {
            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DEV_RESOURCES_ID)
                    .queryParam("domain", TestConstants.UrlRules.GITHUB_DOMAIN)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", equalTo(TestConstants.UrlRules.GITHUB_DOMAIN_ACTIVE_COUNT))
                    .body("id", everyItem(notNullValue()))
                    .body("name", everyItem(notNullValue()))
                    .body("domain", everyItem(equalTo(TestConstants.UrlRules.GITHUB_DOMAIN)))
                    .body("urlPattern", everyItem(notNullValue()))
                    .body("transforms", everyItem(notNullValue()))
                    .body("priority", everyItem(notNullValue()));

        }

        @Test
        @DisplayName("Should return empty list when no rules match the domain")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/url-rules.sql"})
        void shouldReturnEmptyListWhenNoRulesMatchDomain() {
            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DEV_RESOURCES_ID)
                    .queryParam("domain", "nonexistent.com")
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", equalTo(0));
        }

        @Test
        @DisplayName("Should return empty list when fetching inactive url rules")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/url-rules.sql"})
        void shouldReturnEmptyListWhenFetchingInactiveUrlRules() {
            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DEV_RESOURCES_ID)
                    .queryParam("domain", TestConstants.UrlRules.YOUTUBE_DOMAIN)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", equalTo(0));
        }

        @Test
        @DisplayName("Should return empty list when item group does not exist")
        @Sql({"/sql/data/users.sql"})
        void shouldReturnEmptyListWhenItemGroupDoesNotExist() {
            givenNormalUserRequest()
                    .pathParam("groupId", UUID.randomUUID())
                    .queryParam("domain", TestConstants.UrlRules.GITHUB_DOMAIN)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", equalTo(0));
        }

    }

    @Nested
    @DisplayName("General Errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 400 when domain parameter is missing")
        void shouldReturn400WhenDomainIsMissing() {
            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DEV_RESOURCES_ID)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.MISSING_REQUIRED_PARAMETER.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.MISSING_REQUIRED_PARAMETER.getType().toString()));
        }

        @Test
        @DisplayName("Should return 422 when domain parameter is empty")
        void shouldReturn422WhenDomainIsEmpty() {
            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DEV_RESOURCES_ID)
                    .queryParam("domain", " ")
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
        void shouldReturn401WhenUserIsNotAuthenticated() {
            given()
                    .pathParam("groupId", TestConstants.Groups.DEV_RESOURCES_ID)
                    .queryParam("domain", TestConstants.UrlRules.GITHUB_DOMAIN)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.UNAUTHORIZED.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

    }

}
