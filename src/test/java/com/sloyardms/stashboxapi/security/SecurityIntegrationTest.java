package com.sloyardms.stashboxapi.security;

import com.sloyardms.stashboxapi.config.BaseIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles("test")
public class SecurityIntegrationTest extends BaseIntegrationTest {

    @Nested
    @DisplayName("/authenticated")
    class Authenticated {

        @Test
        @DisplayName("Should return 200 and authenticated true")
        void shouldReturn200AndAuthenticated() {
            normalUserRequest()
                    .when()
                    .get("/api/test/authenticated")
                    .then()
                    .statusCode(200)
                    .body("authenticated", equalTo(true));
        }

        @Test
        @DisplayName("Should return 200 and authenticated true when user is admin")
        void shouldReturn200AndTrueWhenUserIsAdmin() {
            adminUserRequest()
                    .when()
                    .get("/api/test/authenticated")
                    .then()
                    .statusCode(200)
                    .body("authenticated", equalTo(true));
        }

        @Test
        @DisplayName("Should return 401 when token is not provided")
        void shouldReturn401WhenTokenIsNotProvided() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/test/authenticated")
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

    @Nested
    @DisplayName("/admin")
    class Admin {

        @Test
        @DisplayName("Should return 200 and admin true when user is admin")
        void shouldReturn200AndTrueWhenUserIsAdmin() {
            adminUserRequest()
                    .when()
                    .get("/api/test/admin")
                    .then()
                    .statusCode(200)
                    .body("admin", equalTo(true));
        }

        @Test
        @DisplayName("Should return 403 when user is not admin")
        void shouldReturn403WhenUserIsNotAdmin() {
            normalUserRequest()
                    .when()
                    .get("/api/test/admin")
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.FORBIDDEN.value());
        }

        @Test
        @DisplayName("Should return 401 when token is not provided")
        void shouldReturn401WhenTokenIsNotProvided() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/test/admin")
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

}
