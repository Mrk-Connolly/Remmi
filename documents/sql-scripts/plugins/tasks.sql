-- ============================================================
-- TASKS PLUGIN SCHEMA
-- ============================================================

DROP TABLE IF EXISTS tasks CASCADE;

CREATE TABLE tasks (
    id              TEXT PRIMARY KEY,
    created         TIMESTAMPTZ NOT NULL,
    modified        TIMESTAMPTZ NOT NULL,
    title           TEXT NOT NULL,
    description     TEXT,
    completed       BOOLEAN NOT NULL DEFAULT FALSE,
    due_date        TIMESTAMPTZ,
    priority        TEXT NOT NULL CHECK (priority IN ('LOW', 'NORMAL', 'HIGH')),
    parent_task     TEXT,
    linked_calendar TEXT,
    reminders       TEXT[] NOT NULL DEFAULT '{}',
    relationships   TEXT[] NOT NULL DEFAULT '{}'
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE tasks TO anon;

ALTER TABLE tasks ENABLE ROW LEVEL SECURITY;

CREATE POLICY "tasks_all" ON tasks FOR ALL TO anon USING (true) WITH CHECK (true);

-- EXAMPLE
INSERT INTO tasks (id, created, modified, title, priority)
VALUES ('task_ex_1', now(), now(), 'Example Task', 'HIGH');
