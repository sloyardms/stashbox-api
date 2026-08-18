-- =====================================================================
-- tag_usage triggers
-- Keeps tag_usage.item_count in sync across:
--   - tag creation
--   - item_tags insert/delete (tag attached/detached from a stash item)
--   - stash_items soft delete / restore (deleted_at transitions)
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Create tag_usage row whenever a tag is created
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_create_tag_usage() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO tag_usage (tag_id, item_count, last_used)
    VALUES (NEW.id, 0, now());
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_create_tag_usage ON tags;
CREATE TRIGGER trg_create_tag_usage
    AFTER INSERT ON tags
    FOR EACH ROW EXECUTE FUNCTION fn_create_tag_usage();

-- ---------------------------------------------------------------------
-- 2. Keep item_count/last_used in sync with item_tags
--    Guarded so that a hard-delete cascading from an already
--    soft-deleted stash_item does NOT double-decrement (the soft-delete
--    trigger already accounted for it).
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_sync_tag_usage() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE tag_usage
        SET item_count = item_count + 1,
            last_used = now()
        WHERE tag_id = NEW.tag_id;

    ELSIF TG_OP = 'DELETE' THEN
        -- Only decrement if the parent stash_item is still active
        -- (not soft-deleted). If it was already soft-deleted, the
        -- soft-delete trigger already decremented this count, so a
        -- later hard-delete/purge must NOT decrement again.
        UPDATE tag_usage
        SET item_count = item_count - 1
        WHERE tag_id = OLD.tag_id
          AND EXISTS (
            SELECT 1 FROM stash_items
            WHERE id = OLD.item_id
              AND deleted_at IS NULL
        );
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_sync_tag_usage ON item_tags;
CREATE TRIGGER trg_sync_tag_usage
    AFTER INSERT OR DELETE ON item_tags
    FOR EACH ROW EXECUTE FUNCTION fn_sync_tag_usage();

-- ---------------------------------------------------------------------
-- 3. Keep item_count in sync when a stash_item is soft-deleted
--    or restored (deleted_at transitions NULL <-> NOT NULL)
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_sync_tag_usage_on_soft_delete() RETURNS TRIGGER AS $$
BEGIN
    -- soft delete: deleted_at went from NULL -> NOT NULL
    IF OLD.deleted_at IS NULL AND NEW.deleted_at IS NOT NULL THEN
        UPDATE tag_usage
        SET item_count = item_count - 1
        WHERE tag_id IN (
            SELECT tag_id FROM item_tags WHERE item_id = NEW.id
        );

        -- restore: deleted_at went from NOT NULL -> NULL
    ELSIF OLD.deleted_at IS NOT NULL AND NEW.deleted_at IS NULL THEN
        UPDATE tag_usage
        SET item_count = item_count + 1,
            last_used = now()
        WHERE tag_id IN (
            SELECT tag_id FROM item_tags WHERE item_id = NEW.id
        );
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_sync_tag_usage_on_soft_delete ON stash_items;
CREATE TRIGGER trg_sync_tag_usage_on_soft_delete
    AFTER UPDATE OF deleted_at ON stash_items
    FOR EACH ROW
    WHEN (OLD.deleted_at IS DISTINCT FROM NEW.deleted_at)
EXECUTE FUNCTION fn_sync_tag_usage_on_soft_delete();