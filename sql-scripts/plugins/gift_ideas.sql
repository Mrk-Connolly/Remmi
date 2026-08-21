-- ============================================================
-- GIFT IDEAS PLUGIN SCHEMA
-- ============================================================

DROP TABLE IF EXISTS gift_ideas CASCADE;

CREATE TABLE gift_ideas (
    id              TEXT PRIMARY KEY,
    created         TIMESTAMPTZ NOT NULL,
    modified        TIMESTAMPTZ NOT NULL,
    contact_id      TEXT NOT NULL REFERENCES contacts(id) ON DELETE CASCADE,
    name            TEXT NOT NULL,
    description     TEXT,
    link            TEXT,
    price           DOUBLE PRECISION,
    event           TEXT CHECK (event IN ('Christmas', 'Birthday', 'FathersDay', 'ValentinesDay', 'MothersDay', 'Anniversary', 'Other'))
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE gift_ideas TO anon;

ALTER TABLE gift_ideas ENABLE ROW LEVEL SECURITY;

CREATE POLICY "gift_ideas_all" ON gift_ideas FOR ALL TO anon USING (true) WITH CHECK (true);

-- EXAMPLE (requires contact_ex_1 from contacts.sql)
INSERT INTO gift_ideas (id, created, modified, contact_id, name, event)
VALUES ('gift_ex_1', now(), now(), 'contact_ex_1', 'Book', 'Birthday');
