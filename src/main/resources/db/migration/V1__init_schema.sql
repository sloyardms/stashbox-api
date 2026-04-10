CREATE TABLE users (
    id UUID PRIMARY KEY,
    settings JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_filters (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    name TEXT NOT NULL,
    domain TEXT NOT NULL,
    url_pattern TEXT NOT NULL,
    extraction_pattern TEXT NOT NULL,
    extraction_group INTEGER NOT NULL DEFAULT 1,
    title_template TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    priority INTEGER NOT NULL DEFAULT 100,
    last_matched_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT user_filters_user_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT user_filters_user_name_unique UNIQUE (user_id, name),
    CONSTRAINT user_filters_user_url_pattern_unique UNIQUE (user_id, url_pattern),
    CONSTRAINT user_filters_priority_positive CHECK (priority >= 0),
    CONSTRAINT user_filters_extraction_group_positive CHECK (extraction_group >= 0)
);

CREATE TABLE item_groups (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    name TEXT NOT NULL,
    slug TEXT NOT NULL,
    description TEXT,
    icon TEXT,
    default_group BOOLEAN NOT NULL DEFAULT true,
    settings JSONB NOT NULL DEFAULT '{}'::jsonb,
    position INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT item_groups_user_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT item_groups_slug_unique UNIQUE (user_id, slug),
    CONSTRAINT item_groups_position_positive_check CHECK (position >= 0)
);
CREATE UNIQUE INDEX item_groups_one_default_group ON item_groups(default_group) WHERE default_group = true;

CREATE TABLE stash_items (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    group_id UUID,
    title TEXT,
    title_normalized TEXT GENERATED ALWAYS AS (lower(trim(regexp_replace(title, '\s+', ' ', 'g')))) STORED,
    url TEXT,
    url_normalized TEXT GENERATED ALWAYS AS (lower(trim(url))) STORED,
    description TEXT,
    image_path TEXT,
    is_favorite BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT stash_items_user_id_fk FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT stash_items_group_id_fk FOREIGN KEY(group_id) REFERENCES item_groups(id) ON DELETE SET NULL

);
CREATE INDEX stash_items_user_group_active_idx ON stash_items(user_id, group_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX stash_items_user_group_deleted_idx ON stash_items(user_id, group_id, deleted_at DESC) WHERE deleted_at IS NOT NULL;
CREATE INDEX stash_items_user_group_title_normalized_idx ON stash_items(user_id, group_id, title_normalized) WHERE deleted_at IS NULL;
CREATE INDEX stash_items_user_group_url_normalized_idx ON stash_items(user_id, group_id, url_normalized) WHERE deleted_at IS NULL;

CREATE TABLE tags (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    group_id UUID NOT NULL,
    name TEXT NOT NULL,
    slug TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT tags_user_id_fk FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT tags_group_id_fk FOREIGN KEY(group_id) REFERENCES item_groups(id) ON DELETE CASCADE,
    CONSTRAINT tags_slug_unique UNIQUE (user_id, group_id, slug)
);

CREATE TABLE item_tags (
    item_id UUID NOT NULL,
    tag_id UUID NOT NULL,

    CONSTRAINT item_tags_stash_item_id_fk FOREIGN KEY(item_id) REFERENCES stash_items(id) ON DELETE CASCADE,
    CONSTRAINT item_tags_tag_id_fk FOREIGN KEY(tag_id) REFERENCES tags(id) ON DELETE CASCADE,

    PRIMARY KEY (item_id, tag_id)
);

CREATE TABLE tag_usage (
    tag_id UUID PRIMARY KEY,
    item_count INTEGER NOT NULL DEFAULT 0,
    last_used TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT tag_usage_tag_id_fk FOREIGN KEY(tag_id) REFERENCES tags(id) ON DELETE CASCADE,
    CONSTRAINT tag_usage_item_count_positive CHECK (item_count >= 0)
);

CREATE TYPE upload_status_enum AS ENUM (
    'PENDING',
    'PROCESSING',
    'COMPLETED',
    'FAILED'
    );

CREATE TABLE item_notes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    item_id UUID NOT NULL,
    content TEXT,
    position INTEGER NOT NULL DEFAULT 0,
    is_pinned BOOLEAN NOT NULL DEFAULT false,
    is_draft BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT item_notes_user_id_fk FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT item_notes_item_id_fk FOREIGN KEY(item_id) REFERENCES stash_items(id) ON DELETE CASCADE
);
CREATE INDEX item_notes_user_item_idx ON item_notes(user_id, item_id);

CREATE TABLE note_files (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    note_id UUID NOT NULL,
    original_filename TEXT NOT NULL,
    stored_filename TEXT NOT NULL,
    file_path TEXT NOT NULL,
    mime_type TEXT NOT NULL,
    file_size BIGINT NOT NULL,
    file_extension TEXT NOT NULL,
    upload_status upload_status_enum NOT NULL DEFAULT 'PENDING',
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT note_files_user_id_fk FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT note_files_item_note_id_fk FOREIGN KEY(note_id) REFERENCES item_notes(id) ON DELETE CASCADE
);
CREATE INDEX note_files_user_note_idx ON note_files(user_id, note_id);

