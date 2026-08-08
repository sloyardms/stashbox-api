package com.sloyardms.stashboxapi.controller.stashitem;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import com.sloyardms.stashboxapi.config.TestConstants;
import com.sloyardms.stashboxapi.domain.stash.model.StashItem;
import com.sloyardms.stashboxapi.domain.stash.repository.StashItemRepository;
import com.sloyardms.stashboxapi.domain.stash.service.StashItemService;
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

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles("test")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class StashItemFavoriteIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/item-groups/{groupSlug}/stash-items/{itemId}/favorite";

    @Autowired
    private StashItemRepository stashItemRepository;

    @Autowired
    private StashItemService stashItemService;

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should change the favorite field and return 204")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql", "/sql/data/stash-items.sql"})
        void shouldChangeFavoriteFieldAndReturn204(){
            StashItem oldItem = stashItemRepository
                    .findById(TestConstants.StashItems.SPRING_BOOT_DOCS_ID)
                            .orElseThrow();
            assertThat(oldItem.getFavorite()).isTrue();

            givenNormalUserRequest()
                    .when()
                    .pathParam("groupSlug", TestConstants.Groups.DEV_RESOURCES_SLUG)
                    .pathParam("itemId", TestConstants.StashItems.SPRING_BOOT_DOCS_ID)
                    .put(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            StashItem newItem = stashItemRepository
                    .findById(TestConstants.StashItems.SPRING_BOOT_DOCS_ID)
                    .orElseThrow();
            assertThat(newItem.getFavorite()).isFalse();
        }

    }

    @Nested
    @DisplayName("General Errors")
    class GeneralErrors {

        @Test
        @DisplayName("Should return 404 when the stash item does not exist")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql"})
        void  shouldReturn404WhenTheStashItemDoesNotExist() {
            assertThat(stashItemRepository.count()).isZero();
            givenNormalUserRequest()
                    .pathParam("groupSlug", TestConstants.Groups.DEV_RESOURCES_SLUG)
                    .pathParam("itemId", UUID.randomUUID())
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
                    .pathParam("groupSlug", TestConstants.Groups.DEV_RESOURCES_SLUG)
                    .pathParam("itemId", TestConstants.StashItems.SPRING_BOOT_DOCS_ID)
                    .when()
                    .put(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.UNAUTHORIZED.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

    }

}
