-- ============================================================
-- CALENDAR PLUGIN SCHEMA
-- ============================================================

DROP TABLE IF EXISTS calendar CASCADE;

CREATE TABLE calendar (
    id              TEXT PRIMARY KEY,
    created         TIMESTAMPTZ NOT NULL,
    modified        TIMESTAMPTZ NOT NULL,
    title           TEXT NOT NULL,
    description     TEXT,
    starting_date   DATE NOT NULL,
    starting_time   TIME,
    ending_date     DATE,
    ending_time     TIME,
    priority        TEXT NOT NULL CHECK (priority IN ('LOW', 'NORMAL', 'HIGH')),
    participants    TEXT[] NOT NULL DEFAULT '{}',
    repeat          TEXT[] NOT NULL DEFAULT '{}',
    location        TEXT[] NOT NULL DEFAULT '{}',
    linked_tasks    TEXT[] NOT NULL DEFAULT '{}',
    linked_alarm    TEXT
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE calendar TO anon;

ALTER TABLE calendar ENABLE ROW LEVEL SECURITY;

CREATE POLICY "calendar_all" ON calendar FOR ALL TO anon USING (true) WITH CHECK (true);

-- EXAMPLE
INSERT INTO calendar (id, created, modified, title, starting_date, priority)
VALUES ('cal_ex_1', now(), now(), 'Example Meeting', CURRENT_DATE + 1, 'NORMAL');
