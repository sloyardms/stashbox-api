-- create tag_usage row whenever a tag is created
CREATE OR REPLACE FUNCTION fn_create_tag_usage() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO tag_usage (tag_id, item_count, last_used)
    VALUES (NEW.id, 0, now());
    return NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_create_tag_usage
AFTER INSERT ON tags
FOR EACH ROW EXECUTE FUNCTION fn_create_tag_usage();

-- keep item_count/last_used in sync with item_tags
CREATE OR REPLACE FUNCTION fn_sync_tag_usage() RETURNS trigger AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE tag_usage
        SET item_count = item_count + 1, last_used = now()
        WHERE tag_id = NEW.tag_id;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE tag_usage
        SET item_count = item_count - 1
        WHERE tag_id = OLD.tag_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sync_tag_usage
AFTER INSERT OR DELETE ON item_tags
FOR EACH ROW EXECUTE FUNCTION fn_sync_tag_usage();