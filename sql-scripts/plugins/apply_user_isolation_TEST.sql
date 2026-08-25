-- ============================================================
-- MIGRATION: per-user isolation for Remmi plugin tables
-- Safe to re-run (uses IF NOT EXISTS / DROP POLICY IF EXISTS).
-- Run this in the Supabase SQL editor (or via the Supabase CLI).
-- ============================================================

-- 1. Add the owner column (defaults to the authenticated user on insert)
ALTER TABLE calendar_TEST    ADD COLUMN IF NOT EXISTS user_id UUID DEFAULT auth.uid();
ALTER TABLE alarms_TEST      ADD COLUMN IF NOT EXISTS user_id UUID DEFAULT auth.uid();
ALTER TABLE tasks_TEST       ADD COLUMN IF NOT EXISTS user_id UUID DEFAULT auth.uid();
ALTER TABLE contacts_TEST    ADD COLUMN IF NOT EXISTS user_id UUID DEFAULT auth.uid();
ALTER TABLE gift_ideas_TEST  ADD COLUMN IF NOT EXISTS user_id UUID DEFAULT auth.uid();

-- 2. Ensure RLS is enabled
ALTER TABLE calendar_TEST   ENABLE ROW LEVEL SECURITY;
ALTER TABLE alarms_TEST     ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks_TEST      ENABLE ROW LEVEL SECURITY;
ALTER TABLE contacts_TEST   ENABLE ROW LEVEL SECURITY;
ALTER TABLE gift_ideas_TEST ENABLE ROW LEVEL SECURITY;

-- 3. Remove the old open policies (covers both the legacy `*_all` naming and the
--    `*_iso` naming from Startup_Complete, so this migration is idempotent).
DROP POLICY IF EXISTS calendar_all    ON calendar_TEST;
DROP POLICY IF EXISTS alarms_all      ON alarms_TEST;
DROP POLICY IF EXISTS tasks_all       ON tasks_TEST;
DROP POLICY IF EXISTS contacts_all    ON contacts_TEST;
DROP POLICY IF EXISTS gift_ideas_all  ON gift_ideas_TEST;
DROP POLICY IF EXISTS calendar_iso    ON calendar_TEST;
DROP POLICY IF EXISTS alarms_iso      ON alarms_TEST;
DROP POLICY IF EXISTS tasks_iso       ON tasks_TEST;
DROP POLICY IF EXISTS contacts_iso    ON contacts_TEST;
DROP POLICY IF EXISTS gift_ideas_iso  ON gift_ideas_TEST;

-- 4. Create strict per-user isolation policies
CREATE POLICY calendar_user_isolation   ON calendar_TEST   FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY alarms_user_isolation     ON alarms_TEST     FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY tasks_user_isolation      ON tasks_TEST      FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY contacts_user_isolation   ON contacts_TEST   FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY gift_ideas_user_isolation ON gift_ideas_TEST FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- 5. (Optional) Backfill existing rows that have no owner.
--    Replace <user-uuid> with the id of the user who should own the legacy data.
--    Without this, rows with NULL user_id are hidden from every user under the
--    strict policies above.
-- UPDATE calendar_TEST   SET user_id = '<user-uuid>' WHERE user_id IS NULL;
-- UPDATE alarms_TEST     SET user_id = '<user-uuid>' WHERE user_id IS NULL;
-- UPDATE tasks_TEST      SET user_id = '<user-uuid>' WHERE user_id IS NULL;
-- UPDATE contacts_TEST   SET user_id = '<user-uuid>' WHERE user_id IS NULL;
-- UPDATE gift_ideas_TEST SET user_id = '<user-uuid>' WHERE user_id IS NULL;
