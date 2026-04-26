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
public class UrlRuleRetrievalIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/item-groups/{groupId}/url-rules/{ruleId}";

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return the url rule and 200")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/url-rules.sql"})
        void shouldReturnUrlRuleAnd200() {
            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DEV_RESOURCES_ID)
                    .pathParam("ruleId", TestConstants.UrlRules.GITHUB_REPO_NAME_ID)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("id", notNullValue())
                    .body("name", equalTo("GitHub Repo Name"))
                    .body("group.name", equalTo("Dev Resources"))
                    .body("description", equalTo("Extracts the repository name from a GitHub URL"))
                    .body("domain", equalTo("github.com"))
                    .body("urlPattern", equalTo("github\\.com/[^/]+/([^/?#]+)"))
                    .body("active", equalTo(true))
                    .body("priority", equalTo(10))
                    .body("transforms", hasSize(2))
                    .body("lastMatchedAt", notNullValue())
                    .body("createdAt", notNullValue())
                    .body("updatedAt", notNullValue());
        }

    }

    @Nested
    @DisplayName("General Errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 404 when the url rule does not exist")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn404WhenTheUrlRuleDoesNotExist() {
            // Doesn't distinguish between "rule not found", "group not found", or "belongs to another user"
            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DEV_RESOURCES_ID)
                    .pathParam("ruleId", UUID.randomUUID())
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .log().body()
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
                    .pathParam("groupId", TestConstants.Groups.DEV_RESOURCES_ID)
                    .pathParam("ruleId", TestConstants.UrlRules.GITHUB_REPO_NAME_ID)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

    }

}
