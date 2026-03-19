-- stash_items full text search (title, url, description)
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

-- users.username and email trigram index
CREATE INDEX users_username_trgm_idx ON users USING GIN (lower(username) gin_trgm_ops);
CREATE INDEX users_email_trgm_idx ON users USING GIN (lower(email) gin_trgm_ops);

-- tags.name trigram index
CREATE INDEX tags_name_trgm_idx ON tags USING GIN (lower(name) gin_trgm_ops);