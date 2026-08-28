-- =====================================================================
-- tag_usage triggers
-- Keeps tag_usage.item_count in sync across:
--   - tag creation
--   - item_tags insert/delete (tag attached/detached from a stash item)
--   - stash_items soft delete / restore (deleted_at transitions)
--   - stash_items hard delete of a still-active item
--
-- Invariant: tag_usage.item_count == number of *non-soft-deleted* stash_items
--            currently linked to the tag.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Create tag_usage row whenever a tag is created
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_create_tag_usage() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO tag_usage (tag_id, item_count, last_used)
    VALUES (NEW.id, 0, now())
    ON CONFLICT (tag_id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_create_tag_usage ON tags;
CREATE TRIGGER trg_create_tag_usage
    AFTER INSERT ON tags
    FOR EACH ROW EXECUTE FUNCTION fn_create_tag_usage();

-- ---------------------------------------------------------------------
-- 2. Keep item_count/last_used in sync with item_tags
--
--    Both branches are guarded on the parent stash_item still being
--    active (deleted_at IS NULL):
--
--    - INSERT: only count the link if the item is active. Attaching a
--      tag to an item that is already in the trash must NOT bump the
--      count (the soft-delete trigger already excluded that item, and
--      the restore trigger will re-add every current tag on restore).
--
--    - DELETE: only decrement if the item is still active. If it was
--      already soft-deleted, the soft-delete trigger already accounted
--      for it, so a later hard-delete/purge cascade must NOT decrement
--      again. During a cascading delete the parent row is already gone,
--      so EXISTS is false and the hard-delete of an *active* item is
--      handled by trigger 4 (BEFORE DELETE) instead.
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_sync_tag_usage() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE tag_usage
        SET item_count = item_count + 1,
            last_used = now()
        WHERE tag_id = NEW.tag_id
          AND EXISTS (
            SELECT 1 FROM stash_items
            WHERE id = NEW.item_id
              AND deleted_at IS NULL
        );

    ELSIF TG_OP = 'DELETE' THEN
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

-- ---------------------------------------------------------------------
-- 4. Keep item_count in sync when a still-active stash_item is
--    hard-deleted (deleted_at IS NULL at delete time).
--
--    Runs BEFORE DELETE so the row - and its item_tags - are still
--    visible. Items that were already soft-deleted are skipped (trigger
--    3 already decremented them). The item_tags cascade that follows
--    hits trigger 2's DELETE branch with the parent row already gone,
--    so it will not double-decrement.
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_sync_tag_usage_on_hard_delete() RETURNS TRIGGER AS $$
BEGIN
    IF OLD.deleted_at IS NULL THEN
        UPDATE tag_usage
        SET item_count = item_count - 1
        WHERE tag_id IN (
            SELECT tag_id FROM item_tags WHERE item_id = OLD.id
        );
    END IF;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_sync_tag_usage_on_hard_delete ON stash_items;
CREATE TRIGGER trg_sync_tag_usage_on_hard_delete
    BEFORE DELETE ON stash_items
    FOR EACH ROW EXECUTE FUNCTION fn_sync_tag_usage_on_hard_delete();
