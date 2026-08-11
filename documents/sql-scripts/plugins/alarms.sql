-- ============================================================
-- ALARMS PLUGIN SCHEMA
-- ============================================================

DROP TABLE IF EXISTS alarms CASCADE;

CREATE TABLE alarms (
    id                    TEXT PRIMARY KEY,
    title                 TEXT NOT NULL,
    description           TEXT,
    priority              TEXT NOT NULL CHECK (priority IN ('LOW', 'NORMAL', 'HIGH')),
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
INSERT INTO alarms (id, title, priority, time)
VALUES ('alarm_ex_1', 'Morning Alarm', 'NORMAL', now() + interval '1 day');
