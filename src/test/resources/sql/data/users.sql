INSERT INTO users (id, settings, created_at, updated_at)
VALUES ('1a9d5015-5330-46af-959b-d6d9913ad75c',
        '{
          "darkMode": true,
          "useFilters": true,
          "testValue": "test"
        }'::jsonb,
        '2025-11-01 09:00:00+00',
        '2025-11-01 09:00:00+00'),
       ('438536b5-335b-4182-9f1b-cc3388b3b707',
        '{
          "darkMode": false,
          "useFilters": true,
          "testValue": "test2"
        }'::jsonb,
        '2024-01-10 07:55:00+00',
        '2024-01-10 07:55:00+00');
