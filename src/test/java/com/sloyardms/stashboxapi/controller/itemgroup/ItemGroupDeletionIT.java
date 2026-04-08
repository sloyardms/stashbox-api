package com.sloyardms.stashboxapi.controller.itemgroup;

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
public class ItemGroupDeletionIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/item-groups/{id}";

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should delete the item group and return 204")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldDeleteTheItemGroup() {
            UUID groupId = TestConstants.USER_GROUP_1_ID;

            givenNormalUserRequest()
                    .when()
                    .pathParam("id", groupId)
                    .delete(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

    }

    @Nested
    @DisplayName("General Errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 404 when the item group does not exist")
        void shouldReturn404WhenItemGroupDoesNotExist() {
            UUID nonExistentGroupId = UUID.randomUUID();

            givenNormalUserRequest()
                    .when()
                    .pathParam("id", nonExistentGroupId)
                    .delete(ENDPOINT)
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
                    .when()
                    .pathParam("id", UUID.randomUUID())
                    .delete(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.UNAUTHORIZED.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

    }

}
