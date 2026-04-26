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
public class UrlRuleCreationIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/item-groups/{groupId}/url-rules";

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return 201 and saved url rule")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn201AndSavedUrlRule() {
            String body = """
                        {
                            "name": "Test Rule",
                            "description": "Matches tags in search",
                            "domain": "www.testsite.com",
                            "urlPattern": "tags=([^&]+)",
                            "priority": 100,
                            "transforms": [
                                { "type": "decode" },
                                { "type": "trim" },
                                { "type": "sentenceCase" },
                                {
                                    "type": "replace",
                                    "from": "_",
                                    "to": " "
                                }
                            ]
                        }
                    """;

            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DESIGN_ID)
                    .body(body)
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("name", equalTo("Test Rule"))
                    .body("description", equalTo("Matches tags in search"))
                    .body("domain", equalTo("www.testsite.com"))
                    .body("urlPattern", equalTo("tags=([^&]+)"))
                    .body("priority", equalTo(100))
                    .body("transforms", hasSize(4))
                    .body("createdAt", notNullValue())
                    .body("updatedAt", notNullValue());
        }

    }

    @Nested
    @DisplayName("General Errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 422 when name is blank")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn422WhenNameIsBlank() {
            String body = """
                        {
                            "description": "Matches tags in search",
                            "domain": "www.testsite.com",
                            "urlPattern": "tags=([^&]+)",
                            "priority": 100,
                            "transforms": [
                                { "type": "decode" },
                                { "type": "trim" },
                                { "type": "sentenceCase" },
                                {
                                    "type": "replace",
                                    "from": "_",
                                    "to": " "
                                }
                            ]
                        }
                    """;

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
        @DisplayName("Should return 422 when name exceeds max length")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn422WhenNameExceedsMaxLength() {
            String body = """
                        {
                            "name": "Name that exceeds max lengthName that exceeds max length",
                            "description": "Matches tags in search",
                            "domain": "www.testsite.com",
                            "urlPattern": "tags=([^&]+)",
                            "priority": 100,
                            "transforms": [
                                { "type": "decode" },
                                { "type": "trim" },
                                { "type": "sentenceCase" },
                                {
                                    "type": "replace",
                                    "from": "_",
                                    "to": " "
                                }
                            ]
                        }
                    """;

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
        @DisplayName("Should return 404 when group does not exist")
        @Sql({"/sql/data/users.sql"})
        void shouldReturn404WhenGroupDoesNotExist() {
            String body = """
                        {
                            "name": "Test Rule",
                            "description": "Matches tags in search",
                            "domain": "www.testsite.com",
                            "urlPattern": "tags=([^&]+)",
                            "priority": 100,
                            "transforms": [
                                { "type": "decode" },
                                { "type": "trim" },
                                { "type": "sentenceCase" },
                                {
                                    "type": "replace",
                                    "from": "_",
                                    "to": " "
                                }
                            ]
                        }
                    """;

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
        @DisplayName("Should return 409 when an url rule with the same name already exist")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/url-rules.sql"})
        void shouldReturn409WhenAnUrlRuleWithTheSameName() {
            String body = """
                        {
                            "name": "GitHub Repo Name",
                            "description": "Matches tags in search",
                            "domain": "www.testsite.com",
                            "urlPattern": "tags=([^&]+)",
                            "priority": 100,
                            "transforms": [
                                { "type": "decode" },
                                { "type": "trim" },
                                { "type": "sentenceCase" },
                                {
                                    "type": "replace",
                                    "from": "_",
                                    "to": " "
                                }
                            ]
                        }
                    """;

            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DEV_RESOURCES_ID)
                    .body(body)
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.DATA_INTEGRITY_VIOLATION.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.DATA_INTEGRITY_VIOLATION.getType().toString()));
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

    }

}
