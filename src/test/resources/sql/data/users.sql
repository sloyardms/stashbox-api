INSERT INTO users (id, settings, created_at, updated_at)
VALUES ('1a9d5015-5330-46af-959b-d6d9913ad75c',
        '{
          "darkModeEnabled": true,
          "filtersEnabled": false
        }'::jsonb,
        '2025-11-01 09:00:00+00',
        '2025-11-01 09:00:00+00'),
       ('438536b5-335b-4182-9f1b-cc3388b3b707',
        '{
          "darkModeEnabled": false,
          "filtersEnabled": true
        }'::jsonb,
        '2024-01-10 07:55:00+00',
        '2024-01-10 07:55:00+00');
