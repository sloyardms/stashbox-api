-- Add triggers to maintain updated_at timestamps on updates for all tables

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- users
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- item_groups
CREATE TRIGGER update_item_groups_updated_at
    BEFORE UPDATE ON item_groups
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- stash_items
CREATE TRIGGER update_stash_items_updated_at
    BEFORE UPDATE ON stash_items
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- tags
CREATE TRIGGER update_tags_updated_at
    BEFORE UPDATE ON tags
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- item_notes
CREATE TRIGGER update_item_notes_updated_at
    BEFORE UPDATE ON item_notes
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- note_files
CREATE TRIGGER update_note_files_updated_at
    BEFORE UPDATE ON note_files
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();