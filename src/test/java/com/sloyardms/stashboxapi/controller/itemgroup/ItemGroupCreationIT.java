package com.sloyardms.stashboxapi.controller.itemgroup;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import com.sloyardms.stashboxapi.domain.stash.dto.request.CreateItemGroupRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.request.CreateItemGroupSettingsRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.response.ItemGroupDetailResponse;
import com.sloyardms.stashboxapi.shared.exception.ErrorCatalog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles("test")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class ItemGroupCreationIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/item-groups";

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return 201 and the saved item group")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn201AndTheSavedItemGroup() {
            CreateItemGroupRequest request = new CreateItemGroupRequest();
            request.setName("Test ItemGroup");
            request.setDescription("Test ItemGroup description");
            request.setIcon("icon");

            CreateItemGroupSettingsRequest settings = new CreateItemGroupSettingsRequest();
            settings.setRequiredTitle(true);
            settings.setUniqueTitle(true);
            request.setSettings(settings);

            ItemGroupDetailResponse savedGroup = givenNormalUserRequest()
                    .body(request)
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .extract().as(ItemGroupDetailResponse.class);

            assertThat(savedGroup).isNotNull();
            assertThat(savedGroup.getName()).isEqualTo(request.getName());
            assertThat(savedGroup.getSlug()).isEqualTo("test-itemgroup");
            assertThat(savedGroup.getDescription()).isEqualTo(request.getDescription());
            assertThat(savedGroup.getIcon()).isEqualTo(request.getIcon());
            assertThat(savedGroup.getPosition()).isEqualTo(4);
            assertThat(savedGroup.getDefaultGroup()).isFalse();
            assertThat(savedGroup.getSettings()).isNotNull();
            assertThat(savedGroup.getSettings().isRequiredTitle()).isEqualTo(request.getSettings().isRequiredTitle());
            assertThat(savedGroup.getSettings().isUniqueTitle()).isEqualTo(request.getSettings().isUniqueTitle());
            // defaults that should always be false unless explicitly set
            assertThat(savedGroup.getSettings().isRequiredImage()).isFalse();
            assertThat(savedGroup.getSettings().isRequiredUrl()).isFalse();
            assertThat(savedGroup.getSettings().isUniqueUrl()).isFalse();
        }

    }

    @Nested
    @DisplayName("General Errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 400 when body is missing")
        void shouldReturn400WhenBodyIsMissing() {
            givenNormalUserRequest()
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.MALFORMED_REQUEST_BODY.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.MALFORMED_REQUEST_BODY.getType().toString()));
        }

        @Test
        @DisplayName("Should return 400 when name is blank")
        void shouldReturn400WhenNameIsBlank() {
            CreateItemGroupRequest request = new CreateItemGroupRequest();
            request.setDescription("Test ItemGroup description");
            request.setIcon("icon");
            request.setSettings(new CreateItemGroupSettingsRequest());

            givenNormalUserRequest()
                    .body(request)
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.VALIDATION_ERROR.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.VALIDATION_ERROR.getType().toString()));
        }

        @Test
        @DisplayName("Should return 400 when name exceeds max length")
        void shouldReturn400WhenNameExceedsMaxLength() {
            CreateItemGroupRequest request = new CreateItemGroupRequest();
            request.setName("T".repeat(100));
            request.setDescription("Test ItemGroup description");
            request.setIcon("icon");
            request.setSettings(new CreateItemGroupSettingsRequest());

            givenNormalUserRequest()
                    .when()
                    .body(request)
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.VALIDATION_ERROR.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.VALIDATION_ERROR.getType().toString()));
        }

        @Test
        @DisplayName("Should return 409 when an item group with the same name already exist")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn409WhenAnItemGroupWithTheSameNameAlreadyExist() {
            CreateItemGroupRequest request = new CreateItemGroupRequest();
            request.setName("Ungrouped");
            request.setDescription("Should fail because Ungrouped default exists");
            request.setIcon("icon");
            request.setSettings(new CreateItemGroupSettingsRequest());

            givenNormalUserRequest()
                    .body(request)
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.DUPLICATE_RESOURCE.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.DUPLICATE_RESOURCE.getType().toString()));
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
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.UNAUTHORIZED.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

    }

}
