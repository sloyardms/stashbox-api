package com.sloyardms.stashboxapi.controller.stashitem;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import com.sloyardms.stashboxapi.config.TestConstants;
import com.sloyardms.stashboxapi.domain.stash.dto.response.StashItemDetailResponse;
import com.sloyardms.stashboxapi.domain.stash.model.StashItem;
import com.sloyardms.stashboxapi.domain.stash.repository.StashItemRepository;
import com.sloyardms.stashboxapi.domain.tag.dto.response.TagCountResponse;
import com.sloyardms.stashboxapi.infrastructure.storage.service.FileStorageService;
import com.sloyardms.stashboxapi.shared.exception.ErrorCatalog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import java.io.File;
import java.time.Instant;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles("test")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class StashItemUpdateIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/item-groups/{groupSlug}/stash-items/{itemId}";

    @Autowired
    private StashItemRepository stashItemRepository;
    @Autowired
    private FileStorageService fileStorageService;

    @AfterEach
    void cleanupStorage() {
        fileStorageService.deleteBaseFolder();
    }

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return 200 and update the stash item")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql", "/sql/data/stash-items.sql"})
        void shouldReturn200AndUpdateTheStashItem() {
            StashItem oldItem = stashItemRepository
                    .findById(TestConstants.StashItems.DOCKER_COMPOSE_CHEAT_SHEET_ID).orElseThrow();
            UUID originalId = oldItem.getId();
            String originalImagePath = oldItem.getImagePath();
            boolean originalFavorite =  oldItem.getFavorite();
            Instant originalDeletedAt = oldItem.getDeletedAt();
            Instant originalCreatedAt = oldItem.getCreatedAt();
            Instant originalUpdatedAt = oldItem.getUpdatedAt();

            File testImage = new File(getClass()
                    .getClassLoader().getResource("data/test-cover.jpg").getFile());

            String body = """
                {
                    "title": "Google",
                    "url": "www.google.com",
                    "description": "Something",
                    "tags": [
                        "Browser",
                        "Account"
                    ],
                    "imageAction": "REPLACE"
                }
                """;

            StashItemDetailResponse response = givenNormalUserMultipartRequest()
                    .pathParam("groupSlug", TestConstants.Groups.UNGROUPED_SLUG)
                    .pathParam("itemId", TestConstants.StashItems.DOCKER_COMPOSE_CHEAT_SHEET_ID)
                    .multiPart("data", body, "application/json")
                    .multiPart("image", testImage, "image/jpeg")
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract().as(StashItemDetailResponse.class);


            // Filesystem
            assertThat(new File(fileStorageService.resolveImagePath(response.getImagePath())))
                    .exists();

            // Response
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(originalId);

            assertThat(response.getTitle()).isEqualTo("Google");
            assertThat(response.getTitleNormalized()).isEqualTo("google");

            assertThat(response.getUrl()).isEqualTo("www.google.com");
            assertThat(response.getUrlNormalized()).isEqualTo("www.google.com");

            assertThat(response.getDescription()).isEqualTo("Something");

            // image was replaced
            assertThat(response.getImagePath()).isNotEqualTo(originalImagePath);
            assertThat(response.getImagePath()).isNotBlank();

            // unchanged fields
            assertThat(response.getFavorite()).isEqualTo(originalFavorite);
            assertThat(response.getDeletedAt()).isEqualTo(originalDeletedAt);
            assertThat(response.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(response.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);

            // Tags
            assertThat(response.getTags())
                    .hasSize(2);
            assertThat(response.getTags())
                    .extracting(TagCountResponse::getName)
                    .containsExactlyInAnyOrder("Browser", "Account");
            assertThat(response.getTags())
                    .extracting(TagCountResponse::getSlug)
                    .containsExactlyInAnyOrder("browser", "account");

            // every returned tag should now have exactly one associated item
            assertThat(response.getTags())
                    .extracting(TagCountResponse::getItemCount)
                    .containsOnly(1);
        }
    }

    @Nested
    @DisplayName("General Errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 404 when stash-item does not exists")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn404WhenStashItemDoesNotExists() {
            String body = """
                {
                    "title": "Google",
                    "url": "www.google.com",
                    "description": "Something",
                    "tags": [
                        "Browser",
                        "Account"
                    ]
                }
                """;

            UUID stashItemId = UUID.randomUUID();
            givenNormalUserMultipartRequest()
                    .pathParam("groupSlug", TestConstants.Groups.UNGROUPED_SLUG)
                    .pathParam("itemId", stashItemId)
                    .multiPart("data", body, "application/json")
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.RESOURCE_NOT_FOUND.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.RESOURCE_NOT_FOUND.getType().toString()));
        }

        @Test
        @DisplayName("Should return 400 when title is required")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql", "/sql/data/stash-items.sql"})
        void shouldReturn400WhenTitleIsRequired() {
            String body = """
                {
                    "title": null,
                    "url": "www.google.com",
                    "description": "Something",
                    "tags": [
                        "Browser",
                        "Account"
                    ]
                }
                """;

            givenNormalUserMultipartRequest()
                    .pathParam("groupSlug", TestConstants.Groups.DEV_RESOURCES_SLUG)
                    .pathParam("itemId", TestConstants.StashItems.SPRING_BOOT_DOCS_ID)
                    .multiPart("data", body, "application/json")
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.VALIDATION_ERROR.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.VALIDATION_ERROR.getType().toString()));
        }

        @Test
        @DisplayName("Should return 400 when url is required")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql", "/sql/data/stash-items.sql"})
        void shouldReturn400WhenUrlIsRequired() {
            String body = """
                {
                    "title": "new title",
                    "url": null,
                    "description": "Something",
                    "tags": [
                        "Browser",
                        "Account"
                    ]
                }
                """;

            givenNormalUserMultipartRequest()
                    .pathParam("groupSlug", TestConstants.Groups.DEV_RESOURCES_SLUG)
                    .pathParam("itemId", TestConstants.StashItems.SPRING_BOOT_DOCS_ID)
                    .multiPart("data", body, "application/json")
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.VALIDATION_ERROR.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.VALIDATION_ERROR.getType().toString()));
        }

        @Test
        @DisplayName("Should return 400 when image is required")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql", "/sql/data/stash-items.sql"})
        void shouldReturn400WhenImageIsRequired() {
            String body = """
                {
                    "title": "new title",
                    "url": "www.google.com",
                    "description": "Something",
                    "tags": [
                        "Browser",
                        "Account"
                    ]
                }
                """;

            givenNormalUserMultipartRequest()
                    .pathParam("groupSlug", TestConstants.Groups.DESIGN_SLUG)
                    .pathParam("itemId", TestConstants.StashItems.MODERN_DASHBOARD_ID)
                    .multiPart("data", body, "application/json")
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.VALIDATION_ERROR.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.VALIDATION_ERROR.getType().toString()));
        }

        @Test
        @DisplayName("Should return 409 when title should be unique")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql", "/sql/data/stash-items.sql"})
        void shouldReturn409WhenTitleShouldBeUnique() {
            // Trying to save a stash item with the same title as TestConstants.StashItems.CREAMY_MUSHROOM_PASTA_ID
            String body = """
                {
                    "title": "Creamy Mushroom Pasta",
                    "url": "www.google.com",
                    "description": "Something",
                    "tags": [
                        "Browser",
                        "Account"
                    ]
                }
                """;

            givenNormalUserMultipartRequest()
                    .pathParam("groupSlug", TestConstants.Groups.RECIPES_SLUG)
                    .pathParam("itemId", TestConstants.StashItems.CLASSIC_BEEF_TACOS_ID)
                    .multiPart("data", body, "application/json")
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.DATA_INTEGRITY_VIOLATION.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.DATA_INTEGRITY_VIOLATION.getType().toString()));
        }

        @Test
        @DisplayName("Should return 409 when url should be unique")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql", "/sql/data/stash-items.sql"})
        void shouldReturn400WhenUrlShouldBeUnique() {
            // Trying to save a stash item with the same url as TestConstants.StashItems.CREAMY_MUSHROOM_PASTA_ID
            String body = """
                {
                    "title": "New name",
                    "url": "https://example.com/mushroom-pasta",
                    "description": "Something",
                    "tags": [
                        "Browser",
                        "Account"
                    ]
                }
                """;

            givenNormalUserMultipartRequest()
                    .pathParam("groupSlug", TestConstants.Groups.RECIPES_SLUG)
                    .pathParam("itemId", TestConstants.StashItems.CLASSIC_BEEF_TACOS_ID)
                    .multiPart("data", body, "application/json")
                    .when()
                    .patch(ENDPOINT)
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
                    .pathParam("groupSlug", TestConstants.Groups.UNGROUPED_SLUG)
                    .pathParam("itemId", TestConstants.StashItems.DOCKER_COMPOSE_CHEAT_SHEET_ID)
                    .when()
                    .patch(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.UNAUTHORIZED.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

    }
}
