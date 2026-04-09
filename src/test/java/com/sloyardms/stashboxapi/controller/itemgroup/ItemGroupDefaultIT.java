package com.sloyardms.stashboxapi.controller.itemgroup;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import com.sloyardms.stashboxapi.config.TestConstants;
import com.sloyardms.stashboxapi.domain.stash.model.ItemGroup;
import com.sloyardms.stashboxapi.domain.stash.repository.ItemGroupRepository;
import com.sloyardms.stashboxapi.shared.exception.ErrorCatalog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles("test")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class ItemGroupDefaultIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/item-groups/{id}/default";
    @Autowired
    private ItemGroupRepository itemGroupRepository;

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should set the group to default and return 204")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldSetGroupToDefaultAndReturn204() {
            UUID itemGroup1Id = TestConstants.USER_GROUP_1_ID;

            givenNormalUserRequest()
                    .pathParam("id", itemGroup1Id)
                    .when()
                    .put(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            ItemGroup oldDefaultGroup = itemGroupRepository.findById(TestConstants.USER_DEFAULT_GROUP_ID).orElse(null);
            assertThat(oldDefaultGroup).isNotNull();
            assertThat(oldDefaultGroup.isDefaultGroup()).isFalse();

            ItemGroup newDefaultGroup = itemGroupRepository.findById(itemGroup1Id).orElse(null);
            assertThat(newDefaultGroup).isNotNull();
            assertThat(newDefaultGroup.isDefaultGroup()).isTrue();
        }

    }

    @Nested
    @DisplayName("General Errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 404 when the item group does not exist")
        void shouldReturn404WhenItemGroupDoesNotExist() {
            UUID nonExistentItemGroupId = UUID.randomUUID();

            givenNormalUserRequest()
                    .pathParam("id", nonExistentItemGroupId)
                    .when()
                    .put(ENDPOINT)
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
                    .pathParam("id", UUID.randomUUID())
                    .when()
                    .put(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.UNAUTHORIZED.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

    }

}
