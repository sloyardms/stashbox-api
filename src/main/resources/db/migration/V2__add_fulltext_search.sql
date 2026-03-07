ALTER TABLE stash_items ADD COLUMN search_vector tsvector
    GENERATED ALWAYS AS (
        to_tsvector('english',
                    coalesce(title, '') || ' ' ||
                    coalesce(url, '') || ' ' ||
                    coalesce(description, '')
        )
        ) STORED;

CREATE INDEX stash_items_search_idx ON stash_items USING GIN(search_vector);

CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX tags_name_trgm_idx ON tags USING GIN (lower(name) gin_trgm_ops);