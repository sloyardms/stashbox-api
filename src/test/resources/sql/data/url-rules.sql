-- Admin user has a github.com rule with the same domain as the normal user.
-- UrlRule b1c2d3e4-0001-4000-8000-000000000005 (YouTube) is inactive
-- Some rules have last_matched_at as null
-- All transform types appear across the rules

-- ==========================================
-- Normal User URL Rules - Dev Resources Group (a1b2c3d4-0002-4000-8000-000000000002)
-- ==========================================
INSERT INTO url_rules (id, user_id, group_id, name, description, domain, url_pattern, transforms, is_active, priority,
                       last_matched_at, created_at, updated_at)
VALUES
    -- GitHub repository rule
    ('b1c2d3e4-0001-4000-8000-000000000001',
     '1a9d5015-5330-46af-959b-d6d9913ad75c',
     'a1b2c3d4-0002-4000-8000-000000000002',
     'GitHub Repo Name',
     'Extracts the repository name from a GitHub URL',
     'github.com',
     'github\.com/[^/]+/([^/?#]+)',
     '[{"type": "trim"}, {"type": "decode"}]'::jsonb,
     true,
     10,
     '2025-02-15 10:30:00+00',
     '2024-11-03 14:30:00+00',
     '2024-11-03 14:30:00+00'),

    -- Stack Overflow question rule
    ('b1c2d3e4-0001-4000-8000-000000000002',
     '1a9d5015-5330-46af-959b-d6d9913ad75c',
     'a1b2c3d4-0002-4000-8000-000000000002',
     'Stack Overflow Question',
     'Extracts the question title slug from Stack Overflow',
     'stackoverflow.com',
     'stackoverflow\.com/questions/\d+/([^/?#]+)',
     '[{"type": "trim"}, {"type": "decode"}, {"type": "replace", "from": "-", "to": " "}, {"type": "sentenceCase"}]'::jsonb,
     true,
     20,
     '2025-03-10 08:15:00+00',
     '2024-11-04 09:00:00+00',
     '2024-11-04 09:00:00+00'),

    -- NPM package rule
    ('b1c2d3e4-0001-4000-8000-000000000003',
     '1a9d5015-5330-46af-959b-d6d9913ad75c',
     'a1b2c3d4-0002-4000-8000-000000000002',
     'NPM Package Name',
     'Extracts the package name from an NPM URL',
     'npmjs.com',
     'npmjs\.com/package/([^/?#]+)',
     '[{"type": "trim"}, {"type": "decode"}]'::jsonb,
     true,
     30,
     null,
     '2024-11-05 10:00:00+00',
     '2024-11-05 10:00:00+00'),

    -- Maven Central artifact rule
    ('b1c2d3e4-0001-4000-8000-000000000004',
     '1a9d5015-5330-46af-959b-d6d9913ad75c',
     'a1b2c3d4-0002-4000-8000-000000000002',
     'Maven Artifact',
     'Extracts the artifact name from Maven Central',
     'mvnrepository.com',
     'mvnrepository\.com/artifact/[^/]+/([^/?#]+)',
     '[{"type": "trim"}, {"type": "decode"}]'::jsonb,
     true,
     40,
     null,
     '2024-11-06 11:00:00+00',
     '2024-11-06 11:00:00+00'),

    -- YouTube video rule (inactive - to test is_active filter)
    ('b1c2d3e4-0001-4000-8000-000000000005',
     '1a9d5015-5330-46af-959b-d6d9913ad75c',
     'a1b2c3d4-0002-4000-8000-000000000002',
     'YouTube Video Title',
     'Extracts video title from YouTube URL - inactive rule',
     'youtube.com',
     'youtube\.com/watch\?.*title=([^&]+)',
     '[{"type": "decode"}, {"type": "trim"}]'::jsonb,
     false,
     50,
     null,
     '2024-11-07 12:00:00+00',
     '2024-11-07 12:00:00+00'),

    -- Medium article rule
    ('b1c2d3e4-0001-4000-8000-000000000006',
     '1a9d5015-5330-46af-959b-d6d9913ad75c',
     'a1b2c3d4-0002-4000-8000-000000000002',
     'Medium Article',
     'Extracts article slug from Medium URL',
     'medium.com',
     'medium\.com/[^/]+/([^/?#]+)-[a-f0-9]+$',
     '[{"type": "trim"}, {"type": "decode"}, {"type": "replace", "from": "-", "to": " "}, {"type": "sentenceCase"}]'::jsonb,
     true,
     60,
     '2025-01-20 14:00:00+00',
     '2024-11-08 13:00:00+00',
     '2024-11-08 13:00:00+00'),

    -- Docker Hub image rule
    ('b1c2d3e4-0001-4000-8000-000000000007',
     '1a9d5015-5330-46af-959b-d6d9913ad75c',
     'a1b2c3d4-0002-4000-8000-000000000002',
     'Docker Hub Image',
     'Extracts image name from Docker Hub',
     'hub.docker.com',
     'hub\.docker\.com/r/[^/]+/([^/?#]+)',
     '[{"type": "trim"}, {"type": "decode"}]'::jsonb,
     true,
     70,
     null,
     '2024-11-09 14:00:00+00',
     '2024-11-09 14:00:00+00');

-- ==========================================
-- Normal User URL Rules - Design Inspiration Group (a1b2c3d4-0003-4000-8000-000000000003)
-- ==========================================
INSERT INTO url_rules (id, user_id, group_id, name, description, domain, url_pattern, transforms, is_active, priority,
                       last_matched_at, created_at, updated_at)
VALUES
    -- Dribbble shot rule
    ('b1c2d3e4-0002-4000-8000-000000000001',
     '1a9d5015-5330-46af-959b-d6d9913ad75c',
     'a1b2c3d4-0003-4000-8000-000000000003',
     'Dribbble Shot',
     'Extracts shot name from Dribbble',
     'dribbble.com',
     'dribbble\.com/shots/\d+-([^/?#]+)',
     '[{"type": "trim"}, {"type": "decode"}, {"type": "replace", "from": "-", "to": " "}, {"type": "sentenceCase"}]'::jsonb,
     true,
     10,
     '2025-02-01 09:00:00+00',
     '2024-11-15 11:00:00+00',
     '2024-11-15 11:00:00+00'),

    -- Behance project rule
    ('b1c2d3e4-0002-4000-8000-000000000002',
     '1a9d5015-5330-46af-959b-d6d9913ad75c',
     'a1b2c3d4-0003-4000-8000-000000000003',
     'Behance Project',
     'Extracts project name from Behance',
     'behance.net',
     'behance\.net/gallery/\d+/([^/?#]+)',
     '[{"type": "trim"}, {"type": "decode"}, {"type": "replace", "from": "-", "to": " "}, {"type": "sentenceCase"}]'::jsonb,
     true,
     20,
     null,
     '2024-11-15 11:01:00+00',
     '2024-11-15 11:01:00+00');

-- ==========================================
-- Normal User URL Rules - Recipes Group (a1b2c3d4-0004-4000-8000-000000000004)
-- ==========================================
INSERT INTO url_rules (id, user_id, group_id, name, description, domain, url_pattern, transforms, is_active, priority,
                       last_matched_at, created_at, updated_at)
VALUES
    -- AllRecipes rule
    ('b1c2d3e4-0003-4000-8000-000000000001',
     '1a9d5015-5330-46af-959b-d6d9913ad75c',
     'a1b2c3d4-0004-4000-8000-000000000004',
     'AllRecipes Title',
     'Extracts recipe name from AllRecipes URL',
     'allrecipes.com',
     'allrecipes\.com/recipe/\d+/([^/?#]+)',
     '[{"type": "trim"}, {"type": "decode"}, {"type": "replace", "from": "-", "to": " "}, {"type": "sentenceCase"}]'::jsonb,
     true,
     10,
     '2025-03-05 19:00:00+00',
     '2024-12-01 18:20:00+00',
     '2024-12-01 18:20:00+00');

-- ==========================================
-- Admin User URL Rules - for cross-user isolation tests (a1b2c3d4-0005-4000-8000-000000000005)
-- ==========================================
INSERT INTO url_rules (id, user_id, group_id, name, description, domain, url_pattern, transforms, is_active, priority,
                       last_matched_at, created_at, updated_at)
VALUES
    ('b1c2d3e4-0004-4000-8000-000000000001',
     '438536b5-335b-4182-9f1b-cc3388b3b707',
     'a1b2c3d4-0005-4000-8000-000000000005',
     'Admin GitHub Rule',
     'Admin user GitHub repo extractor',
     'github.com',
     'github\.com/[^/]+/([^/?#]+)',
     '[{"type": "trim"}]'::jsonb,
     true,
     10,
     null,
     '2025-01-10 07:55:00+00',
     '2025-01-10 07:55:00+00');