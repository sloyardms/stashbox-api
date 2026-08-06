package com.sloyardms.stashboxapi.controller.stashitem;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import com.sloyardms.stashboxapi.config.TestConstants;
import com.sloyardms.stashboxapi.domain.stash.model.StashItem;
import com.sloyardms.stashboxapi.domain.stash.repository.StashItemRepository;
import com.sloyardms.stashboxapi.infrastructure.storage.service.FileStorageService;
import com.sloyardms.stashboxapi.shared.exception.ErrorCatalog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import static org.assertj.core.api.Assertions.assertThat;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class StashItemDeletionIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/item-groups/{groupSlug}/stash-items/{itemId}";

    @MockitoBean
    private FileStorageService fileStorageService;
    @Autowired
    private StashItemRepository stashItemRepository;

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should delete the stash item and return 204")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql", "/sql/data/stash-items.sql"})
        void shouldDeleteTheStashItemAndReturn204() {
            // Logical deletion
            givenNormalUserRequest()
                    .when()
                    .pathParam("groupSlug", TestConstants.Groups.DEV_RESOURCES_SLUG)
                    .pathParam("itemId", TestConstants.StashItems.SPRING_BOOT_DOCS_ID)
                    .delete(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            // Verify the item deletedAt is not null and the method was not called
            StashItem item = stashItemRepository
                    .findById(TestConstants.StashItems.SPRING_BOOT_DOCS_ID)
                    .orElseThrow();
            assertThat(item.getDeletedAt()).isNotNull();
            verify(fileStorageService, never())
                    .deleteStashItemFolder(any(), any());

            // Hard deletion
            givenNormalUserRequest()
                    .when()
                    .pathParam("groupSlug", TestConstants.Groups.DEV_RESOURCES_SLUG)
                    .pathParam("itemId", TestConstants.StashItems.SPRING_BOOT_DOCS_ID)
                    .delete(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            // Verify the item was deleted and the method was called
            assertThat(stashItemRepository.findById(TestConstants.StashItems.SPRING_BOOT_DOCS_ID)).isEmpty();
            verify(fileStorageService)
                    .deleteStashItemFolder(
                            TestConstants.Users.NORMAL_USER_ID,
                            TestConstants.StashItems.SPRING_BOOT_DOCS_ID
                    );
        }

    }

    @Nested
    @DisplayName("General Errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 404 when the stash item does not exist")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void  shouldReturn404WhenTheStashItemDoesNotExist() {
            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.DEV_RESOURCES_SLUG)
                    .pathParam("itemId", TestConstants.StashItems.SPRING_BOOT_DOCS_ID)
                    .when()
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
                    .pathParam("groupSlug", TestConstants.Groups.DEV_RESOURCES_SLUG)
                    .pathParam("itemId", TestConstants.StashItems.SPRING_BOOT_DOCS_ID)
                    .when()
                    .delete(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.UNAUTHORIZED.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

    }

}
