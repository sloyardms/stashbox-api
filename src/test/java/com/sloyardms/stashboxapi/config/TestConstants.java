package com.sloyardms.stashboxapi.config;

import java.util.UUID;

public final class TestConstants {

    private TestConstants() {
    }

    public static final class Users {
        private Users() {
        }

        public static final UUID NORMAL_USER_ID = UUID.fromString("1a9d5015-5330-46af-959b-d6d9913ad75c");
        public static final UUID ADMIN_USER_ID = UUID.fromString("438536b5-335b-4182-9f1b-cc3388b3b707");
    }

    public static final class Groups {
        private Groups() {
        }

        // Normal user IDs (keep these for repository lookups if needed)
        public static final UUID UNGROUPED_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000001");
        public static final UUID DEV_RESOURCES_ID = UUID.fromString("a1b2c3d4-0002-4000-8000-000000000002");

        public static final String UNGROUPED_SLUG = "ungrouped";
        public static final String DEV_RESOURCES_SLUG = "dev-resources";
        public static final String DESIGN_SLUG = "design-inspiration";
        public static final String RECIPES_SLUG = "recipes";

        // Admin user
        public static final String ADMIN_UNGROUPED_SLUG = "ungrouped";

        // Counts
        public static final int NORMAL_USER_COUNT = 4;
        public static final int ADMIN_USER_COUNT = 1;

    }

    public static final class Tags {
        private Tags() {
        }

        // Dev Resources
        public static final String DOCKER_SLUG = "docker";
        public static final String KUBERNETES_SLUG = "kubernetes";
        public static final String SPRING_BOOT_SLUG = "spring-boot";
        public static final String SPRING_SECURITY_SLUG = "spring-security";
        public static final String JAVA_SLUG = "java";
        public static final String POSTGRESQL_SLUG = "postgresql";
        public static final String REDIS_SLUG = "redis";
        public static final String GIT_SLUG = "git";
        public static final String LINUX_SLUG = "linux";
        public static final String TESTING_SLUG = "testing";
        public static final String TESTS_SLUG = "tests";
        public static final String CICD_SLUG = "ci-cd";
        public static final String YAML_SLUG = "yaml";
        public static final String REST_API_SLUG = "rest-api";
        public static final String MICROSERVICES_SLUG = "microservices";

        // Design Inspiration
        public static final String UI_SLUG = "ui";
        public static final String UX_SLUG = "ux";
        public static final String TYPOGRAPHY_SLUG = "typography";
        public static final String COLOR_PALETTE_SLUG = "color-palette";
        public static final String FIGMA_SLUG = "figma";

        // Recipes
        public static final String ITALIAN_SLUG = "italian";
        public static final String VEGAN_SLUG = "vegan";
        public static final String QUICK_MEALS_SLUG = "quick-meals";

        // Admin user
        public static final String ADMIN_DOCKER_SLUG = "admin-docker";
        public static final String ADMIN_JAVA_SLUG = "admin-java";

        // Counts
        public static final int DEV_RESOURCES_COUNT = 15;
        public static final int DESIGN_COUNT = 5;
        public static final int RECIPES_COUNT = 3;
    }

    public static final class UrlRules {
        private UrlRules() {
        }

        // Dev Resources
        public static final UUID GITHUB_REPO_NAME_ID = UUID.fromString("b1c2d3e4-0001-4000-8000-000000000001");
        public static final UUID STACK_OVERFLOW_QUESTION_ID = UUID.fromString("b1c2d3e4-0001-4000-8000-000000000002");
        public static final UUID NPM_PACKAGE_NAME_ID = UUID.fromString("b1c2d3e4-0001-4000-8000-000000000003");
        public static final UUID MAVEN_ARTIFACT_ID = UUID.fromString("b1c2d3e4-0001-4000-8000-000000000004");
        public static final UUID YOUTUBE_VIDEO_TITLE_ID = UUID.fromString("b1c2d3e4-0001-4000-8000-000000000005"); //
        // inactive
        public static final UUID MEDIUM_ARTICLE_ID = UUID.fromString("b1c2d3e4-0001-4000-8000-000000000006");
        public static final UUID DOCKER_HUB_IMAGE_ID = UUID.fromString("b1c2d3e4-0001-4000-8000-000000000007");

        // Design Inspiration
        public static final UUID DRIBBBLE_SHOT_ID = UUID.fromString("b1c2d3e4-0002-4000-8000-000000000001");
        public static final UUID BEHANCE_PROJECT_ID = UUID.fromString("b1c2d3e4-0002-4000-8000-000000000002");

        // Recipes
        public static final UUID ALL_RECIPES_TITLE_ID = UUID.fromString("b1c2d3e4-0003-4000-8000-000000000001");

        // Admin user
        public static final UUID ADMIN_GITHUB_RULE_ID = UUID.fromString("b1c2d3e4-0004-4000-8000-000000000001");

        // Domains
        public static final String GITHUB_DOMAIN = "github.com";
        public static final String STACK_OVERFLOW_DOMAIN = "stackoverflow.com";
        public static final String NPM_DOMAIN = "npmjs.com";
        public static final String MAVEN_DOMAIN = "mvnrepository.com";
        public static final String YOUTUBE_DOMAIN = "youtube.com";
        public static final String MEDIUM_DOMAIN = "medium.com";
        public static final String DOCKER_HUB_DOMAIN = "hub.docker.com";
        public static final String DRIBBBLE_DOMAIN = "dribbble.com";
        public static final String BEHANCE_DOMAIN = "behance.net";
        public static final String ALL_RECIPES_DOMAIN = "allrecipes.com";

        // Counts
        public static final int NORMAL_USER_URLRULE_COUNT = 10;
        public static final int DEV_RESOURCES_COUNT = 7;
        public static final int DEV_RESOURCES_ACTIVE_COUNT = 6;
        public static final int DESIGN_COUNT = 2;
        public static final int RECIPES_COUNT = 1;
        public static final int ADMIN_COUNT = 1;
        public static final int GITHUB_DOMAIN_ACTIVE_COUNT = 1;
    }
}
