CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Generated tsvector column combining title, url, description with weights
ALTER TABLE stash_items
    ADD COLUMN search_vector tsvector
        GENERATED ALWAYS AS (
            setweight(to_tsvector('simple', coalesce(title_normalized, '')), 'A') ||
            setweight(to_tsvector('simple', coalesce(url_normalized, '')), 'C') ||
            setweight(to_tsvector('simple', coalesce(description, '')), 'D')
            ) STORED;

CREATE INDEX idx_stash_items_search_vector ON stash_items USING GIN (search_vector);