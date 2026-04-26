package com.sloyardms.stashboxapi.controller.rules;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import com.sloyardms.stashboxapi.config.TestConstants;
import com.sloyardms.stashboxapi.domain.rules.model.UrlRule;
import com.sloyardms.stashboxapi.domain.rules.repository.UrlRuleRepository;
import com.sloyardms.stashboxapi.shared.exception.ErrorCatalog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles("test")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class UrlRuleUpdateMatchedIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/item-groups/{groupId}/url-rules/{ruleId}/matched";
    @Autowired
    private UrlRuleRepository urlRuleRepository;

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should update 'lastMatchedAt' and return 204")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/url-rules.sql"})
        void shouldUpdateLastMatchedAtAndReturn204() {
            Optional<UrlRule> existingRule = urlRuleRepository.findById(TestConstants.UrlRules.NPM_PACKAGE_NAME_ID);
            assertThat(existingRule).isPresent();
            assertThat(existingRule.get().getLastMatchedAt()).isNull();

            givenNormalUserRequest()
                    .pathParam("groupId", TestConstants.Groups.DEV_RESOURCES_ID)
                    .pathParam("ruleId", TestConstants.UrlRules.NPM_PACKAGE_NAME_ID)
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            Optional<UrlRule> modifiedRule = urlRuleRepository.findById(TestConstants.UrlRules.NPM_PACKAGE_NAME_ID);
            assertThat(modifiedRule).isPresent();
            assertThat(modifiedRule.get().getLastMatchedAt()).isNotNull();
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
                    .patch(ENDPOINT)
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
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

    }

}
