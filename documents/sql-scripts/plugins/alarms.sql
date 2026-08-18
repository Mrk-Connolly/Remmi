-- ============================================================
-- ALARMS PLUGIN SCHEMA
-- ============================================================

DROP TABLE IF EXISTS alarms CASCADE;

CREATE TABLE alarms (
    id                    TEXT PRIMARY KEY,
    created               TIMESTAMPTZ NOT NULL,
    modified              TIMESTAMPTZ NOT NULL,
    title                 TEXT NOT NULL,
    description           TEXT,
    is_priority           BOOLEAN NOT NULL DEFAULT FALSE,
    linked_calendar_event TEXT,
    linked_task           TEXT,
    time                  TIMESTAMPTZ NOT NULL,
    repeatable            TEXT[] NOT NULL DEFAULT '{}',
    custom                TEXT[] NOT NULL DEFAULT '{}'
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE alarms TO anon;

ALTER TABLE alarms ENABLE ROW LEVEL SECURITY;

CREATE POLICY "alarms_all" ON alarms FOR ALL TO anon USING (true) WITH CHECK (true);

-- EXAMPLE
INSERT INTO alarms (id, created, modified, title, is_priority, time)
VALUES ('alarm_ex_1', now(), now(), 'Morning Alarm', FALSE, now() + interval '1 day');
