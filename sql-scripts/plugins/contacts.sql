-- ============================================================
-- CONTACTS PLUGIN SCHEMA
-- ============================================================

DROP TABLE IF EXISTS contacts CASCADE;

CREATE TABLE contacts (
    id              TEXT PRIMARY KEY,
    created         TIMESTAMPTZ NOT NULL,
    modified        TIMESTAMPTZ NOT NULL,
    user_id         UUID DEFAULT auth.uid(),
    name            TEXT NOT NULL,
    surname         TEXT NOT NULL,
    nickname        TEXT,
    mobile_phone    TEXT,
    email           TEXT,
    birthday        TEXT,
    group_name      TEXT NOT NULL,
    is_favorite     BOOLEAN NOT NULL DEFAULT FALSE,
    in_gift_list    BOOLEAN NOT NULL DEFAULT FALSE
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE contacts TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE contacts TO anon;

ALTER TABLE contacts ENABLE ROW LEVEL SECURITY;

CREATE POLICY "contacts_user_isolation" ON contacts FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- EXAMPLE
INSERT INTO contacts (id, created, modified, name, surname, group_name, is_favorite, in_gift_list)
VALUES ('contact_ex_1', now(), now(), 'Example', 'User', 'General', true, false);
