-- ============================================================
-- MIGRATION: per-user isolation for Remmi plugin tables
-- Safe to re-run (uses IF NOT EXISTS / DROP POLICY IF EXISTS).
-- Run this in the Supabase SQL editor (or via the Supabase CLI).
-- ============================================================

-- 1. Add the owner column (defaults to the authenticated user on insert)
ALTER TABLE calendar    ADD COLUMN IF NOT EXISTS user_id UUID DEFAULT auth.uid();
ALTER TABLE alarms      ADD COLUMN IF NOT EXISTS user_id UUID DEFAULT auth.uid();
ALTER TABLE tasks       ADD COLUMN IF NOT EXISTS user_id UUID DEFAULT auth.uid();
ALTER TABLE contacts    ADD COLUMN IF NOT EXISTS user_id UUID DEFAULT auth.uid();
ALTER TABLE gift_ideas  ADD COLUMN IF NOT EXISTS user_id UUID DEFAULT auth.uid();

-- 2. Ensure RLS is enabled
ALTER TABLE calendar   ENABLE ROW LEVEL SECURITY;
ALTER TABLE alarms     ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks      ENABLE ROW LEVEL SECURITY;
ALTER TABLE contacts   ENABLE ROW LEVEL SECURITY;
ALTER TABLE gift_ideas ENABLE ROW LEVEL SECURITY;

-- 3. Remove the old open policies (covers both the legacy `*_all` naming and the
--    `*_iso` naming from Startup_Complete, so this migration is idempotent).
DROP POLICY IF EXISTS calendar_all    ON calendar;
DROP POLICY IF EXISTS alarms_all      ON alarms;
DROP POLICY IF EXISTS tasks_all       ON tasks;
DROP POLICY IF EXISTS contacts_all    ON contacts;
DROP POLICY IF EXISTS gift_ideas_all  ON gift_ideas;
DROP POLICY IF EXISTS calendar_iso    ON calendar;
DROP POLICY IF EXISTS alarms_iso      ON alarms;
DROP POLICY IF EXISTS tasks_iso       ON tasks;
DROP POLICY IF EXISTS contacts_iso    ON contacts;
DROP POLICY IF EXISTS gift_ideas_iso  ON gift_ideas;

-- 4. Create strict per-user isolation policies
CREATE POLICY calendar_user_isolation   ON calendar   FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY alarms_user_isolation     ON alarms     FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY tasks_user_isolation      ON tasks      FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY contacts_user_isolation   ON contacts   FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY gift_ideas_user_isolation ON gift_ideas FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- 5. (Optional) Backfill existing rows that have no owner.
--    Replace <user-uuid> with the id of the user who should own the legacy data.
--    Without this, rows with NULL user_id are hidden from every user under the
--    strict policies above.
-- UPDATE calendar   SET user_id = '<user-uuid>' WHERE user_id IS NULL;
-- UPDATE alarms     SET user_id = '<user-uuid>' WHERE user_id IS NULL;
-- UPDATE tasks      SET user_id = '<user-uuid>' WHERE user_id IS NULL;
-- UPDATE contacts   SET user_id = '<user-uuid>' WHERE user_id IS NULL;
-- UPDATE gift_ideas SET user_id = '<user-uuid>' WHERE user_id IS NULL;
