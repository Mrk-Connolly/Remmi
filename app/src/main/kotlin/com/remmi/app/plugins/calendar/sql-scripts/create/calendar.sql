-- ============================================================
-- CALENDAR PLUGIN SCHEMA
-- ============================================================

DROP TABLE IF EXISTS calendar CASCADE;

CREATE TABLE calendar (
    id              TEXT PRIMARY KEY,
    created         TIMESTAMPTZ NOT NULL,
    modified         TIMESTAMPTZ NOT NULL,
    user_id         UUID DEFAULT auth.uid(),
    title           TEXT NOT NULL,
    description     TEXT,
    starting_date   DATE NOT NULL,
    starting_time   TIME,
    ending_date     DATE,
    ending_time     TIME,
    is_priority     BOOLEAN NOT NULL DEFAULT FALSE,
    group_name      TEXT,
    participants    TEXT[] NOT NULL DEFAULT '{}',
    repeat          TEXT[] NOT NULL DEFAULT '{}',
    location        TEXT[] NOT NULL DEFAULT '{}',
    linked_tasks    TEXT[] NOT NULL DEFAULT '{}',
    linked_alarm    TEXT
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE calendar TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE calendar TO anon;

ALTER TABLE calendar ENABLE ROW LEVEL SECURITY;

CREATE POLICY "calendar_user_isolation" ON calendar FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- EXAMPLE
INSERT INTO calendar (id, created, modified, title, starting_date, is_priority, group_name)
VALUES ('cal_ex_1', now(), now(), 'Example Meeting', CURRENT_DATE + 1, FALSE, 'Work');
