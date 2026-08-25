-- MIGRATION V2: Linked-Item Relationships Refactor

-- 1. Update CALENDAR table
ALTER TABLE calendar ADD COLUMN create_alarm BOOLEAN DEFAULT FALSE;
ALTER TABLE calendar ADD COLUMN create_task BOOLEAN DEFAULT FALSE;

-- Migrate intent flags from existing IDs
UPDATE calendar SET create_alarm = TRUE WHERE linked_alarm IS NOT NULL;
UPDATE calendar SET create_task = TRUE WHERE linked_tasks IS NOT NULL AND array_length(linked_tasks, 1) > 0;

-- 2. Update ALARMS table
ALTER TABLE alarms ADD COLUMN source_plugin TEXT;
ALTER TABLE alarms ADD COLUMN source_item_id TEXT;

-- Migrate existing relationships
UPDATE alarms SET source_plugin = 'calendar', source_item_id = linked_calendar_event WHERE linked_calendar_event IS NOT NULL;
UPDATE alarms SET source_plugin = 'tasks', source_item_id = linked_task WHERE linked_task IS NOT NULL;

-- 3. Update TASKS table
ALTER TABLE tasks ADD COLUMN source_plugin TEXT;
ALTER TABLE tasks ADD COLUMN source_item_id TEXT;
ALTER TABLE tasks ADD COLUMN create_calendar BOOLEAN DEFAULT FALSE;
ALTER TABLE tasks ADD COLUMN create_alarm BOOLEAN DEFAULT FALSE;

-- Migrate existing relationships
UPDATE tasks SET source_plugin = 'calendar', source_item_id = linked_calendar WHERE linked_calendar IS NOT NULL;

-- 4. Update GIFT_IDEAS table
ALTER TABLE gift_ideas ADD COLUMN source_plugin TEXT;
ALTER TABLE gift_ideas ADD COLUMN source_item_id TEXT;

-- Migrate existing contact relationship
UPDATE gift_ideas SET source_plugin = 'contacts', source_item_id = contact_id WHERE contact_id IS NOT NULL;

-- 5. Add Indexes for performance
CREATE INDEX idx_alarms_source ON alarms(source_plugin, source_item_id);
CREATE INDEX idx_tasks_source ON tasks(source_plugin, source_item_id);
CREATE INDEX idx_gift_ideas_source ON gift_ideas(source_plugin, source_item_id);

-- 6. Cleanup (Optional: keep columns but make them nullable/deprecated if preferred)
ALTER TABLE alarms DROP COLUMN linked_calendar_event;
ALTER TABLE alarms DROP COLUMN linked_task;
ALTER TABLE calendar DROP COLUMN linked_tasks;
ALTER TABLE calendar DROP COLUMN linked_alarm;
ALTER TABLE tasks DROP COLUMN linked_calendar;
