-- ============================================================
-- GIFT IDEAS PLUGIN SCHEMA
-- ============================================================

DROP TABLE IF EXISTS gift_ideas CASCADE;

CREATE TABLE gift_ideas (
    id              TEXT PRIMARY KEY,
    created         TIMESTAMPTZ NOT NULL,
    modified        TIMESTAMPTZ NOT NULL,
    user_id         UUID DEFAULT auth.uid(),
    contact_id      TEXT NOT NULL REFERENCES contacts(id) ON DELETE CASCADE,
    name            TEXT NOT NULL,
    description     TEXT,
    link            TEXT,
    price           DOUBLE PRECISION,
    event           TEXT CHECK (event IN ('Christmas', 'Birthday', 'FathersDay', 'ValentinesDay', 'MothersDay', 'Anniversary', 'Other'))
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE gift_ideas TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE gift_ideas TO anon;

ALTER TABLE gift_ideas ENABLE ROW LEVEL SECURITY;

CREATE POLICY "gift_ideas_user_isolation" ON gift_ideas FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- EXAMPLE (requires contact_ex_1 from contacts.sql)
INSERT INTO gift_ideas (id, created, modified, contact_id, name, event)
VALUES ('gift_ex_1', now(), now(), 'contact_ex_1', 'Book', 'Birthday');
