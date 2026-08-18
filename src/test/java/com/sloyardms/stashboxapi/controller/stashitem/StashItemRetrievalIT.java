package com.sloyardms.stashboxapi.controller.stashitem;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import com.sloyardms.stashboxapi.config.TestConstants;
import com.sloyardms.stashboxapi.domain.stash.dto.response.StashItemDetailResponse;
import com.sloyardms.stashboxapi.domain.tag.dto.response.TagCountResponse;
import com.sloyardms.stashboxapi.shared.exception.ErrorCatalog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import java.time.Instant;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles("test")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class StashItemRetrievalIT extends BaseIntegrationTest {

    private final String ENDPOINT = "/api/v1/stash-items/{itemId}";

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return 200 and the stash item")
        @Sql(scripts = {"/sql/data/users.sql", "/sql/data/item-groups.sql", "/sql/data/tags.sql", "/sql/data/stash-items.sql"})
        void shouldReturn200AndTheStashItem() {
            StashItemDetailResponse response = givenNormalUserRequest()
                    .pathParam("itemId", TestConstants.StashItems.JAVA_STREAMS_GUIDE_ID)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract().as(StashItemDetailResponse.class);

            assertThat(response).isNotNull();
            assertThat(response.getId())
                    .isEqualTo(UUID.fromString("b1c2d3e4-0001-4000-8000-000000000006"));
            assertThat(response.getTitle())
                    .isEqualTo("Java Streams Guide");
            assertThat(response.getTitleNormalized())
                    .isEqualTo("java streams guide");
            assertThat(response.getUrl())
                    .isEqualTo("https://www.baeldung.com/java-8-streams");
            assertThat(response.getUrlNormalized())
                    .isEqualTo("https://www.baeldung.com/java-8-streams");
            assertThat(response.getDescription())
                    .isEqualTo("Reference guide covering Java Streams, collections and testing examples.");
            assertThat(response.getImagePath())
                    .isEqualTo("/images/java-streams.jpg");
            assertThat(response.getFavorite()).isFalse();
            assertThat(response.getDeletedAt()).isNull();
            assertThat(response.getCreatedAt())
                    .isEqualTo(Instant.parse("2025-02-20T11:00:00Z"));
            assertThat(response.getUpdatedAt())
                    .isEqualTo(Instant.parse("2025-02-20T11:00:00Z"));
            assertThat(response.getGroup()).isNotNull();
            assertThat(response.getGroup().getSlug())
                    .isEqualTo(TestConstants.Groups.UNGROUPED_SLUG);
            assertThat(response.getTags())
                    .hasSize(2);
            assertThat(response.getTags())
                    .extracting(TagCountResponse::getName)
                    .containsExactlyInAnyOrder("Java", "Testing");
            assertThat(response.getTags())
                    .extracting(TagCountResponse::getSlug)
                    .containsExactlyInAnyOrder("java", "testing");
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
                    .pathParam("itemId", TestConstants.StashItems.JAVA_STREAMS_GUIDE_ID)
                    .when()
                    .get(ENDPOINT)
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
                    .pathParam("itemId", TestConstants.StashItems.JAVA_STREAMS_GUIDE_ID)
                    .when()
                    .get(ENDPOINT)
                    .then()
                    .log().body()
                    .statusCode(ErrorCatalog.UNAUTHORIZED.getStatus().value())
                    .body("type", equalTo(ErrorCatalog.UNAUTHORIZED.getType().toString()));
        }

    }

}
