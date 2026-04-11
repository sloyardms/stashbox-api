package com.sloyardms.stashboxapi.controller.itemgroup;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import com.sloyardms.stashboxapi.config.TestConstants;
import com.sloyardms.stashboxapi.domain.stash.dto.response.ItemGroupDetailResponse;
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
public class ItemGroupUpdateIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/item-groups/{id}";

    @Autowired
    private ItemGroupRepository itemGroupRepository;

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return 200 and update the item group")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn200AndUpdateTheItemGroup() {
            ItemGroup itemGroup1 = itemGroupRepository.findById(TestConstants.Groups.DEV_RESOURCES_ID).orElse(null);
            assertThat(itemGroup1).isNotNull();

            String request = """
                    {
                      "name": "new name",
                      "icon": "new icon",
                      "settings": {
                        "requiredTitle": false,
                        "uniqueTitle": false
                      }
                    }
                    """;

            ItemGroupDetailResponse response = givenNormalUserRequest()
                    .body(request)
                    .pathParam("id", TestConstants.Groups.DEV_RESOURCES_ID)
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract().as(ItemGroupDetailResponse.class);

            //ItemGroup
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(itemGroup1.getId());
            assertThat(response.getName()).isEqualTo("new name");
            assertThat(response.getSlug()).isEqualTo("new-name");
            assertThat(response.getIcon()).isEqualTo("new icon");
            assertThat(response.getDefaultGroup()).isEqualTo(itemGroup1.isDefaultGroup());
            assertThat(response.getPosition()).isEqualTo(itemGroup1.getPosition());
            assertThat(response.getCreatedAt()).isNotNull();
            assertThat(response.getUpdatedAt()).isNotNull();
            //ItemGroup Settings
            assertThat(response.getSettings()).isNotNull();
            assertThat(response.getSettings().isRequiredTitle()).isFalse();
            assertThat(response.getSettings().isUniqueTitle()).isFalse();
            assertThat(response.getSettings().isRequiredUrl()).isTrue();
            assertThat(response.getSettings().isUniqueUrl()).isTrue();
            assertThat(response.getSettings().isRequiredImage()).isFalse();
        }

    }

    @Nested
    @DisplayName("General Errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 404 when the group does not exist")
        @Sql({"/sql/data/users.sql"})
        void shouldReturn404WhenTheGroupDoesNotExist() {
            String request = """
                    {
                      "name": "test"
                    }
                    """;

            givenNormalUserRequest()
                    .pathParam("id", TestConstants.Groups.DEV_RESOURCES_ID)
                    .body(request)
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.RESOURCE_NOT_FOUND.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.RESOURCE_NOT_FOUND.getType().toString()));
        }

        @Test
        @DisplayName("Should return 409 when group name already exist")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn409WhenTheGroupNameAlreadyExists() {
            String request = """
                    {
                      "name": "Ungrouped"
                    }
                    """;

            givenNormalUserRequest()
                    .pathParam("id", TestConstants.Groups.DEV_RESOURCES_ID)
                    .body(request)
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.DUPLICATE_RESOURCE.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.DUPLICATE_RESOURCE.getType().toString()));
        }

        @Test
        @DisplayName("Should return 422 when name is set to blank")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn422WhenNameIsSetToBlank() {
            String request = """
                    {
                      "name": null
                    }
                    """;

            givenNormalUserRequest()
                    .pathParam("id", TestConstants.Groups.DEV_RESOURCES_ID)
                    .body(request)
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.VALIDATION_ERROR.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.VALIDATION_ERROR.getType().toString()));
        }

        @Test
        @DisplayName("Should return 422 when name exceeds max length")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn422WhenNameExceedsMaxLength() {
            String name = "N".repeat(100);
            String request = String.format("{ \"name\": \"%s\" }", name);

            givenNormalUserRequest()
                    .pathParam("id", TestConstants.Groups.DEV_RESOURCES_ID)
                    .body(request)
                    .when()
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
                    .pathParam("id", UUID.randomUUID())
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.UNAUTHORIZED.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

    }

}
