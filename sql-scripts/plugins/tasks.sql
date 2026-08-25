-- ============================================================
-- TASKS PLUGIN SCHEMA
-- ============================================================

DROP TABLE IF EXISTS tasks CASCADE;

CREATE TABLE tasks (
    id              TEXT PRIMARY KEY,
    created         TIMESTAMPTZ NOT NULL,
    modified        TIMESTAMPTZ NOT NULL,
    user_id         UUID DEFAULT auth.uid(),
    title           TEXT NOT NULL,
    description     TEXT,
    completed       BOOLEAN NOT NULL DEFAULT FALSE,
    due_date        TIMESTAMPTZ,
    is_priority     BOOLEAN NOT NULL DEFAULT FALSE,
    group_name      TEXT,
    parent_task     TEXT,
    linked_calendar TEXT,
    repeat          JSONB,
    reminders       TEXT[] NOT NULL DEFAULT '{}',
    relationships   TEXT[] NOT NULL DEFAULT '{}'
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE tasks TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE tasks TO anon;

ALTER TABLE tasks ENABLE ROW LEVEL SECURITY;

CREATE POLICY "tasks_user_isolation" ON tasks FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- EXAMPLE
INSERT INTO tasks (id, created, modified, title, is_priority, group_name)
VALUES ('task_ex_1', now(), now(), 'Example Task', TRUE, 'Work');
