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

        // Normal user
        public static final UUID UNGROUPED_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000001");
        public static final UUID DEV_RESOURCES_ID = UUID.fromString("a1b2c3d4-0002-4000-8000-000000000002");
        public static final UUID DESIGN_ID = UUID.fromString("a1b2c3d4-0003-4000-8000-000000000003");
        public static final UUID RECIPES_ID = UUID.fromString("a1b2c3d4-0004-4000-8000-000000000004");

        // Admin user
        public static final UUID ADMIN_UNGROUPED_ID = UUID.fromString("a1b2c3d4-0005-4000-8000-000000000005");

        // Counts
        public static final int NORMAL_USER_COUNT = 4;
        public static final int ADMIN_USER_COUNT = 1;

    }

    public static final class Tags {
        private Tags() {
        }

        // Dev Resources
        public static final UUID DOCKER_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000001");
        public static final UUID KUBERNETES_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000002");
        public static final UUID SPRING_BOOT_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000003");
        public static final UUID SPRING_SECURITY_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000004");
        public static final UUID JAVA_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000005");
        public static final UUID POSTGRESQL_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000006");
        public static final UUID REDIS_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000007");
        public static final UUID GIT_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000008");
        public static final UUID LINUX_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000009");
        public static final UUID TESTING_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000010");
        public static final UUID TESTS_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000011");
        public static final UUID CICD_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000012");
        public static final UUID YAML_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000013");
        public static final UUID REST_API_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000014");
        public static final UUID MICROSERVICES_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000015");

        // Design Inspiration
        public static final UUID UI_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000016");
        public static final UUID UX_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000017");
        public static final UUID TYPOGRAPHY_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000018");
        public static final UUID COLOR_PALETTE_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000019");
        public static final UUID FIGMA_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000020");

        // Recipes
        public static final UUID ITALIAN_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000021");
        public static final UUID VEGAN_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000022");
        public static final UUID QUICK_MEALS_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000023");

        // Admin user
        public static final UUID ADMIN_DOCKER_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000101");
        public static final UUID ADMIN_JAVA_ID = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000102");

        // Counts
        public static final int DEV_RESOURCES_COUNT = 15;
        public static final int DESIGN_COUNT = 5;
        public static final int RECIPES_COUNT = 3;
    }
}
