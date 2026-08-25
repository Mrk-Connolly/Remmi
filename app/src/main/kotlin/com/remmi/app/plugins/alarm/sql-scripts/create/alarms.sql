-- ============================================================
-- ALARMS PLUGIN SCHEMA
-- ============================================================

DROP TABLE IF EXISTS alarms CASCADE;

CREATE TABLE alarms (
    id                    TEXT PRIMARY KEY,
    created               TIMESTAMPTZ NOT NULL,
    modified              TIMESTAMPTZ NOT NULL,
    user_id               UUID DEFAULT auth.uid(),
    title                 TEXT NOT NULL,
    description           TEXT,
    is_priority           BOOLEAN NOT NULL DEFAULT FALSE,
    linked_calendar_event TEXT,
    linked_task           TEXT,
    time                  TIMESTAMPTZ NOT NULL,
    repeatable            TEXT[] NOT NULL DEFAULT '{}',
    custom                TEXT[] NOT NULL DEFAULT '{}'
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE alarms TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE alarms TO anon;

ALTER TABLE alarms ENABLE ROW LEVEL SECURITY;

CREATE POLICY "alarms_user_isolation" ON alarms FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- EXAMPLE
INSERT INTO alarms (id, created, modified, title, is_priority, time)
VALUES ('alarm_ex_1', now(), now(), 'Morning Alarm', FALSE, now() + interval '1 day');
