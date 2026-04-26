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
import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles("test")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class UrlRuleUpdateIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/item-groups/{groupId}/url-rules/{ruleId}";

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return 200 and update the url rule")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/url-rules.sql"})
        void shouldReturn200AndUpdateTheUrlRule() {
            String newName = "Bitbucket Repo Name";
            String newDescription = "Extracts the repository name from a Bitbucket URL";
            String newDomain = "bitbucket.com";

            String body = String.format("""
                    {
                        "name": "%s",
                        "description": "%s",
                        "domain": "%s"
                    }
                    """, newName, newDescription, newDomain);

            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DEV_RESOURCES_ID)
                    .pathParam("ruleId", TestConstants.UrlRules.GITHUB_REPO_NAME_ID)
                    .body(body)
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(
                            "id", equalTo(TestConstants.UrlRules.GITHUB_REPO_NAME_ID.toString()),
                            "group.id", equalTo(TestConstants.Groups.DEV_RESOURCES_ID.toString()),
                            "name", equalTo(newName),
                            "description", equalTo(newDescription),
                            "domain", equalTo(newDomain),
                            "urlPattern", equalTo("github\\.com/[^/]+/([^/?#]+)"),
                            "active", equalTo(true),
                            "priority", equalTo(10),
                            "lastMatchedAt", equalTo("2025-02-15T10:30:00Z"),
                            "createdAt", equalTo("2024-11-03T14:30:00Z"),
                            "updatedAt", equalTo("2024-11-03T14:30:00Z")
                    );
        }

    }

    @Nested
    @DisplayName("General Errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 404 when the url rule does not exist")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn404WhenTheUrlRuleDoesNotExist() {
            String request = """
                    {
                        "name": "new name"
                    }
                    """;

            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DEV_RESOURCES_ID)
                    .pathParam("ruleId", UUID.randomUUID())
                    .body(request)
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.RESOURCE_NOT_FOUND.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.RESOURCE_NOT_FOUND.getType().toString()));
        }

        @Test
        @DisplayName("Should return 409 when url rule already exist")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/url-rules.sql"})
        void shouldReturn409WhenTheUrlRuleAlreadyExist() {
            String request = """
                    {
                        "name": "GitHub Repo Name"
                    }
                    """;

            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DEV_RESOURCES_ID)
                    .pathParam("ruleId", TestConstants.UrlRules.STACK_OVERFLOW_QUESTION_ID)
                    .body(request)
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.DATA_INTEGRITY_VIOLATION.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.DATA_INTEGRITY_VIOLATION.getType().toString()));
        }

        @Test
        @DisplayName("Should return 422 when name is blank")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/url-rules.sql"})
        void shouldReturn422WhenNameIsBlank() {
            String request = """
                    {
                        "name": ""
                    }
                    """;

            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DEV_RESOURCES_ID)
                    .pathParam("ruleId", TestConstants.UrlRules.STACK_OVERFLOW_QUESTION_ID)
                    .body(request)
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.VALIDATION_ERROR.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.VALIDATION_ERROR.getType().toString()));
        }

        @Test
        @DisplayName("Should return 422 when name exceeds max length")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/url-rules.sql"})
        void shouldReturn422WhenNameExceedsMaxLength() {
            String name = "t".repeat(100);
            String request = String.format("""
                    {
                        "name": "%s"
                    }
                    """, name);

            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DEV_RESOURCES_ID)
                    .pathParam("ruleId", TestConstants.UrlRules.STACK_OVERFLOW_QUESTION_ID)
                    .body(request)
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
                    .pathParam("groupId", TestConstants.Groups.DEV_RESOURCES_ID)
                    .pathParam("ruleId", TestConstants.UrlRules.GITHUB_REPO_NAME_ID)
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.UNAUTHORIZED.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

    }
}
