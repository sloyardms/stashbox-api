-- ==========================================
-- STASH ITEMS - Normal User
-- User: 478f7eba-d161-4d33-b058-2bc4553110ad
-- ==========================================
-- 5 normal-user items across all groups (Ungrouped: 2, Dev Resources: 1, Design Inspiration 1

INSERT INTO stash_items (
    id,
    user_id,
    group_id,
    title,
    title_normalized,
    url,
    url_normalized,
    description,
    image_path,
    is_favorite,
    deleted_at,
    created_at,
    updated_at
)
VALUES
-- Dev Resources
(
    'b1c2d3e4-0001-4000-8000-000000000001',
    '478f7eba-d161-4d33-b058-2bc4553110ad',
    'a1b2c3d4-0002-4000-8000-000000000002',
    'Spring Boot Documentation',
    'spring boot documentation',
    'https://docs.spring.io/spring-boot',
    'https://docs.spring.io/spring-boot',
    'Official Spring Boot reference documentation.',
    NULL,
    true,
    NULL,
    '2025-01-15 10:00:00+00',
    '2025-01-15 10:00:00+00'
),

(
    'b1c2d3e4-0001-4000-8000-000000000002',
    '478f7eba-d161-4d33-b058-2bc4553110ad',
    'a1b2c3d4-0002-4000-8000-000000000002',
    'PostgreSQL Docs',
    'postgresql docs',
    'https://www.postgresql.org/docs/',
    'https://www.postgresql.org/docs/',
    'PostgreSQL official documentation.',
    NULL,
    false,
    NULL,
    '2025-01-16 11:30:00+00',
    '2025-01-16 11:30:00+00'
),
-- Ungrouped
(
    'b1c2d3e4-0001-4000-8000-000000000003',
    '478f7eba-d161-4d33-b058-2bc4553110ad',
    'a1b2c3d4-0001-4000-8000-000000000001',
    'Docker Compose Cheat Sheet',
    'docker compose cheat sheet',
    'https://docs.docker.com/compose/',
    'https://docs.docker.com/compose/',
    'Useful Docker Compose commands for local development and testing.',
    NULL,
    false,
    NULL,
    '2025-02-18 09:00:00+00',
    '2025-02-18 09:00:00+00'
),
(
    'b1c2d3e4-0001-4000-8000-000000000004',
    '478f7eba-d161-4d33-b058-2bc4553110ad',
    'a1b2c3d4-0001-4000-8000-000000000001',
    'Testing REST APIs',
    'testing rest apis',
    'https://www.postman.com/api-platform/',
    'https://www.postman.com/api-platform/',
    'Examples and notes for integration testing REST APIs with Postman.',
    NULL,
    true,
    NULL,
    '2025-02-19 10:00:00+00',
    '2025-02-19 10:00:00+00'
),
(
    'b1c2d3e4-0001-4000-8000-000000000005',
    '478f7eba-d161-4d33-b058-2bc4553110ad',
    'a1b2c3d4-0001-4000-8000-000000000001',
    'Interesting Article',
    'interesting article',
    'https://example.com/article',
    'https://example.com/article',
    'Random article saved for later.',
    NULL,
    false,
    NULL,
    '2025-02-15 08:45:00+00',
    '2025-02-15 08:45:00+00'
),
(
    'b1c2d3e4-0001-4000-8000-000000000006',
    '478f7eba-d161-4d33-b058-2bc4553110ad',
    'a1b2c3d4-0001-4000-8000-000000000001',
    'Java Streams Guide',
    'java streams guide',
    'https://www.baeldung.com/java-8-streams',
    'https://www.baeldung.com/java-8-streams',
    'Reference guide covering Java Streams, collections and testing examples.',
    '/images/java-streams.jpg',
    false,
    NULL,
    '2025-02-20 11:00:00+00',
    '2025-02-20 11:00:00+00'
),

-- Design Inspiration
(
    'b1c2d3e4-0001-4000-8000-000000000007',
    '478f7eba-d161-4d33-b058-2bc4553110ad',
    'a1b2c3d4-0003-4000-8000-000000000003',
    'Modern Dashboard Inspiration',
    'modern dashboard inspiration',
    'https://dribbble.com/',
    'https://dribbble.com/',
    'Dashboard ideas collected from Dribbble.',
    '/images/dashboard-inspiration.png',
    true,
    NULL,
    '2025-02-01 09:00:00+00',
    '2025-02-01 09:00:00+00'
),

-- Recipes
(
    'b1c2d3e4-0001-4000-8000-000000000008',
    '478f7eba-d161-4d33-b058-2bc4553110ad',
    'a1b2c3d4-0004-4000-8000-000000000004',
    'Creamy Mushroom Pasta',
    'creamy mushroom pasta',
    'https://example.com/mushroom-pasta',
    'https://example.com/mushroom-pasta',
    'Recipe to try this weekend.',
    '/images/mushroom-pasta.jpg',
    false,
    NULL,
    '2025-02-10 18:15:00+00',
    '2025-02-10 18:15:00+00'
),
(
    'b1c2d3e4-0001-4000-8000-000000000009',
    '478f7eba-d161-4d33-b058-2bc4553110ad',
    'a1b2c3d4-0004-4000-8000-000000000004',
    'Classic Beef Tacos',
    'classic beef tacos',
    'https://example.com/beef-tacos',
    'https://example.com/beef-tacos',
    'Easy homemade tacos with seasoned beef and fresh toppings.',
    '/images/beef-tacos.jpg',
    false,
    NULL,
    '2025-02-12 19:30:00+00',
    '2025-02-12 19:30:00+00'
);

-- Ungrouped
;

-- ==========================================
-- STASH ITEMS - Admin User
-- User: 573e407f-3ab7-4495-aceb-b310938542e5
-- ==========================================

INSERT INTO stash_items (
    id,
    user_id,
    group_id,
    title,
    title_normalized,
    url,
    url_normalized,
    description,
    image_path,
    is_favorite,
    deleted_at,
    created_at,
    updated_at
)
VALUES
    (
        'b1c2d3e4-0001-4000-8000-000000000101',
        '573e407f-3ab7-4495-aceb-b310938542e5',
        'a1b2c3d4-0005-4000-8000-000000000005',
        'Internal Admin Guide',
        'internal admin guide',
        'https://internal.example.com/admin-guide',
        'https://internal.example.com/admin-guide',
        'Admin-only documentation.',
        NULL,
        true,
        NULL,
        '2025-03-01 09:00:00+00',
        '2025-03-01 09:00:00+00'
    ),

    (
        'b1c2d3e4-0001-4000-8000-000000000102',
        '573e407f-3ab7-4495-aceb-b310938542e5',
        'a1b2c3d4-0005-4000-8000-000000000005',
        'Server Runbook',
        'server runbook',
        'https://internal.example.com/runbook',
        'https://internal.example.com/runbook',
        'Operations checklist.',
        NULL,
        false,
        NULL,
        '2025-03-02 09:30:00+00',
        '2025-03-02 09:30:00+00'
    );

-- ==========================================
-- ITEM TAGS
-- ==========================================

INSERT INTO item_tags (item_id, tag_id)
VALUES

-- Spring Boot Documentation
('b1c2d3e4-0001-4000-8000-000000000001', 'a1b2c3d4-0001-4000-8000-000000000003'), -- Spring Boot
('b1c2d3e4-0001-4000-8000-000000000001', 'a1b2c3d4-0001-4000-8000-000000000005'), -- Java
('b1c2d3e4-0001-4000-8000-000000000001', 'a1b2c3d4-0001-4000-8000-000000000014'), -- REST API

-- PostgreSQL Docs
('b1c2d3e4-0001-4000-8000-000000000002', 'a1b2c3d4-0001-4000-8000-000000000006'), -- PostgreSQL
('b1c2d3e4-0001-4000-8000-000000000002', 'a1b2c3d4-0001-4000-8000-000000000007'), -- Redis

-- Docker Compose Cheat Sheet
('b1c2d3e4-0001-4000-8000-000000000003', 'a1b2c3d4-0001-4000-8000-000000000001'), -- Docker
('b1c2d3e4-0001-4000-8000-000000000003', 'a1b2c3d4-0001-4000-8000-000000000013'), -- YAML
('b1c2d3e4-0001-4000-8000-000000000003', 'a1b2c3d4-0001-4000-8000-000000000009'), -- Linux

-- Testing REST APIs
('b1c2d3e4-0001-4000-8000-000000000004', 'a1b2c3d4-0001-4000-8000-000000000010'), -- Testing
('b1c2d3e4-0001-4000-8000-000000000004', 'a1b2c3d4-0001-4000-8000-000000000011'), -- Tests
('b1c2d3e4-0001-4000-8000-000000000004', 'a1b2c3d4-0001-4000-8000-000000000014'), -- REST API

-- Interesting article
('b1c2d3e4-0001-4000-8000-000000000005', 'a1b2c3d4-0001-4000-8000-000000000008'), -- Git
('b1c2d3e4-0001-4000-8000-000000000005', 'a1b2c3d4-0001-4000-8000-000000000009'), -- Linux

-- Java Streams Guide
('b1c2d3e4-0001-4000-8000-000000000006', 'a1b2c3d4-0001-4000-8000-000000000005'), -- Java
('b1c2d3e4-0001-4000-8000-000000000006', 'a1b2c3d4-0001-4000-8000-000000000010'), -- Testing

-- Modern Dashboard
('b1c2d3e4-0001-4000-8000-000000000007', 'a1b2c3d4-0001-4000-8000-000000000016'), -- UI
('b1c2d3e4-0001-4000-8000-000000000007', 'a1b2c3d4-0001-4000-8000-000000000017'), -- UX
('b1c2d3e4-0001-4000-8000-000000000007', 'a1b2c3d4-0001-4000-8000-000000000020'), -- Figma

-- Creamy Mushroom Pasta
('b1c2d3e4-0001-4000-8000-000000000008', 'a1b2c3d4-0001-4000-8000-000000000021'), -- Italian
('b1c2d3e4-0001-4000-8000-000000000008', 'a1b2c3d4-0001-4000-8000-000000000023'), -- Quick Meals

-- Creamy Mushroom Pasta
('b1c2d3e4-0001-4000-8000-000000000009', 'a1b2c3d4-0001-4000-8000-000000000023'), -- Quick Meals

-- Admin items
('b1c2d3e4-0001-4000-8000-000000000101', 'a1b2c3d4-0001-4000-8000-000000000101'), -- Admin Docker
('b1c2d3e4-0001-4000-8000-000000000102', 'a1b2c3d4-0001-4000-8000-000000000102'); -- Admin Java