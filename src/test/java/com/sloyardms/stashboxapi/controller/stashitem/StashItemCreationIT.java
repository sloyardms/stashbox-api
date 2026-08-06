package com.sloyardms.stashboxapi.controller.stashitem;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import com.sloyardms.stashboxapi.config.TestConstants;
import com.sloyardms.stashboxapi.domain.stash.dto.request.CreateStashItemRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.response.StashItemDetailResponse;
import com.sloyardms.stashboxapi.domain.tag.repository.TagRepository;
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
import java.util.HashSet;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles("test")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class StashItemCreationIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/item-groups/{groupSlug}/stash-items";

    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private TagRepository tagRepository;

    @AfterEach
    void cleanupStorage() {
        fileStorageService.deleteBaseFolder();
    }

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return 201 and saved stash item with image")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn201AndTheSavedStashItemWithImage() {
            CreateStashItemRequest body = new CreateStashItemRequest();
            body.setTitle("Salsa Macha Recipe");
            body.setUrl("https://www.thekitchn.com/salsa-macha-recipe-23736329");
            body.setDescription("Smoky/crunchy salsa");
            HashSet<String> tags = new HashSet<>(Set.of("Vegan","Salsa"));
            body.setTags(tags);

            File testImage = new File(getClass()
                    .getClassLoader().getResource("data/test-cover.jpg").getFile());

            String groupSLug = TestConstants.Groups.RECIPES_SLUG;
            StashItemDetailResponse response = givenNormalUserMultipartRequest()
                    .pathParam("groupSlug", groupSLug)
                    .multiPart("data", body, "application/json")
                    .multiPart("image", testImage, "image/jpeg")
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .extract()
                    .as(StashItemDetailResponse.class);

            assertThat(response.getTitle()).isEqualTo(body.getTitle());
            assertThat(response.getTitleNormalized()).isNotNull();
            assertThat(response.getUrl()).isEqualTo(body.getUrl());
            assertThat(response.getUrlNormalized()).isNotNull();
            assertThat(response.getDescription()).isEqualTo(body.getDescription());
            assertThat(response.getGroup().getName()).isNotNull();
            assertThat(response.getGroup().getSlug()).isEqualTo(groupSLug);
            assertThat(response.getImagePath()).isNotNull();
            assertThat(response.getFavorite()).isFalse();
            assertThat(response.getDeletedAt()).isNull();
            assertThat(response.getCreatedAt()).isNotNull();
            assertThat(response.getUpdatedAt()).isNotNull();

            //check cover file exist
            assertThat(new File(fileStorageService.resolveImagePath(response.getImagePath()))).exists();

            //check if tags exists
            assertThat(tagRepository.count()).isEqualTo(tags.size());
        }

        @Test
        @DisplayName("Should return 201 and saved stash item without image")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn201AndTheSavedStashItemWithoutImage() {
            CreateStashItemRequest body = new CreateStashItemRequest();
            body.setTitle("Java tutorial");
            body.setUrl("https://www.example.com/java-tutorial-23736329");
            body.setDescription("Java 25 Tutorial");
            HashSet<String> tags = new HashSet<>(Set.of("Java","Tutorial"));
            body.setTags(tags);

            String groupSLug = TestConstants.Groups.DEV_RESOURCES_SLUG;
            StashItemDetailResponse response = givenNormalUserMultipartRequest()
                    .pathParam("groupSlug", groupSLug)
                    .multiPart("data", body, "application/json")
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .extract()
                    .as(StashItemDetailResponse.class);

            assertThat(response.getTitle()).isEqualTo(body.getTitle());
            assertThat(response.getTitleNormalized()).isNotNull();
            assertThat(response.getUrl()).isEqualTo(body.getUrl());
            assertThat(response.getUrlNormalized()).isNotNull();
            assertThat(response.getDescription()).isEqualTo(body.getDescription());
            assertThat(response.getGroup().getName()).isNotNull();
            assertThat(response.getGroup().getSlug()).isEqualTo(groupSLug);
            assertThat(response.getImagePath()).isNull();
            assertThat(response.getFavorite()).isFalse();
            assertThat(response.getDeletedAt()).isNull();
            assertThat(response.getCreatedAt()).isNotNull();
            assertThat(response.getUpdatedAt()).isNotNull();

            //check if tags exists
            assertThat(tagRepository.count()).isEqualTo(tags.size());
        }

    }

    @Nested
    @DisplayName("General Errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 400 when title is required")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn400WhenTitleIsRequired() {
            CreateStashItemRequest body = new CreateStashItemRequest();
            body.setUrl("https://www.somesite.com/dev-tutorial-23736329");
            body.setDescription("Description");
            HashSet<String> tags = new HashSet<>(Set.of("Java","Tutorial"));
            body.setTags(tags);

            // Dev resources require each stash item to have a title
            String groupSLug = TestConstants.Groups.DEV_RESOURCES_SLUG;

            givenNormalUserMultipartRequest()
                    .pathParam("groupSlug", groupSLug)
                    .multiPart("data", body, "application/json")
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.VALIDATION_ERROR.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.VALIDATION_ERROR.getType().toString()));
        }

        @Test
        @DisplayName("Should return 400 when url is required")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql", "/sql/data/stash-items.sql"})
        void shouldReturn400WhenUrlIsRequired() {
            CreateStashItemRequest body = new CreateStashItemRequest();
            body.setTitle("Some tutorial");
            body.setDescription("Description");
            HashSet<String> tags = new HashSet<>(Set.of("Java","Tutorial"));
            body.setTags(tags);

            // Dev resources require each stash item to have an url
            String groupSLug = TestConstants.Groups.DEV_RESOURCES_SLUG;

            givenNormalUserMultipartRequest()
                    .pathParam("groupSlug", groupSLug)
                    .multiPart("data", body, "application/json")
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.VALIDATION_ERROR.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.VALIDATION_ERROR.getType().toString()));
        }

        @Test
        @DisplayName("Should return 400 when image is required")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql", "/sql/data/stash-items.sql"})
        void shouldReturn400WhenImageIsRequired() {
            CreateStashItemRequest body = new CreateStashItemRequest();
            body.setTitle("Some tutorial");
            body.setDescription("Description");

            // Design group require each stash item to have an image
            String groupSLug = TestConstants.Groups.DESIGN_SLUG;

            givenNormalUserMultipartRequest()
                    .pathParam("groupSlug", groupSLug)
                    .multiPart("data", body, "application/json")
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.VALIDATION_ERROR.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.VALIDATION_ERROR.getType().toString()));
        }

        @Test
        @DisplayName("Should return 409 when title should be unique")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn409WhenTitleShouldBeUnique() {
            CreateStashItemRequest body = new CreateStashItemRequest();
            body.setTitle("Test title");
            body.setUrl("https://www.somesite.com/some-dev-tutorial-23736329");

            // Dev resources require each stash item to have a title, and each title must be unique
            String groupSLug = TestConstants.Groups.DEV_RESOURCES_SLUG;

            givenNormalUserMultipartRequest()
                    .pathParam("groupSlug", groupSLug)
                    .multiPart("data", body, "application/json")
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.CREATED.value());

            givenNormalUserMultipartRequest()
                    .pathParam("groupSlug", groupSLug)
                    .multiPart("data", body, "application/json")
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.DATA_INTEGRITY_VIOLATION.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.DATA_INTEGRITY_VIOLATION.getType().toString()));
        }

        @Test
        @DisplayName("Should return 409 when url should be unique")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn409WhenUrlShouldBeUnique() {
            CreateStashItemRequest body = new CreateStashItemRequest();
            body.setTitle("Test title");
            body.setUrl("https://www.somesite.com/some-dev-tutorial-23736329");

            // Recipes require each stash item to have a url, and each url must be unique
            String groupSLug = TestConstants.Groups.RECIPES_SLUG;

            givenNormalUserMultipartRequest()
                    .pathParam("groupSlug", groupSLug)
                    .multiPart("data", body, "application/json")
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.CREATED.value());

            body = new CreateStashItemRequest();
            body.setTitle("Another title");
            body.setUrl("https://www.somesite.com/some-dev-tutorial-23736329");

            givenNormalUserMultipartRequest()
                    .pathParam("groupSlug", groupSLug)
                    .multiPart("data", body, "application/json")
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.DATA_INTEGRITY_VIOLATION.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.DATA_INTEGRITY_VIOLATION.getType().toString()));
        }

        @Test
        @DisplayName("Should return 400 when at least one of title, url, description, or image is required")
        @Sql({"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void shouldReturn400WhenAtLeastOneOfTitleDescriptionOrImageIsRequired() {
            CreateStashItemRequest body = new CreateStashItemRequest();
            HashSet<String> tags = new HashSet<>(Set.of("Vegan","Salsa"));
            body.setTags(tags);

            String groupSLug = TestConstants.Groups.DEV_RESOURCES_SLUG;

            givenNormalUserMultipartRequest()
                    .pathParam("groupSlug", groupSLug)
                    .multiPart("data", body, "application/json")
                    .when()
                    .post(ENDPOINT)
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
                    .pathParam("groupSlug", TestConstants.Groups.DESIGN_SLUG)
                    .when()
                    .post(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.UNAUTHORIZED.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

    }
}
