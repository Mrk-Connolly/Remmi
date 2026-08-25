-- ============================================================
-- REMMI DATABASE BOOTSTRAP (COMPLETE)
-- Includes: Calendar, Tasks, Alarms, Contacts, Gift List,
--           Recipe Book, and Ingredient Stock.
--
-- Aligned with Kotlin Models & Multi-User Architecture (RLS)
-- ============================================================

-- ============================================================
-- 1. CLEANUP
-- ============================================================

DROP TABLE IF EXISTS stock_batches_TEST CASCADE;
DROP TABLE IF EXISTS user_stock_TEST CASCADE;
DROP TABLE IF EXISTS ingredient_metadata_TEST CASCADE;
DROP TABLE IF EXISTS recipes_TEST CASCADE;
DROP TABLE IF EXISTS gift_ideas_TEST CASCADE;
DROP TABLE IF EXISTS contacts_TEST CASCADE;
DROP TABLE IF EXISTS alarms_TEST CASCADE;
DROP TABLE IF EXISTS tasks_TEST CASCADE;
DROP TABLE IF EXISTS calendar_TEST CASCADE;
DROP TABLE IF EXISTS database_tests_TEST CASCADE;


-- ============================================================
-- 2. CALENDAR
-- ============================================================

CREATE TABLE calendar_TEST (
    id              TEXT PRIMARY KEY,
    created         TIMESTAMPTZ NOT NULL,
    modified        TIMESTAMPTZ NOT NULL,
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


-- ============================================================
-- 3. TASKS
-- ============================================================

CREATE TABLE tasks_TEST (
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


-- ============================================================
-- 4. ALARMS
-- ============================================================

CREATE TABLE alarms_TEST (
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
    custom                TEXT[] NOT NULL DEFAULT '{}',
    use_sound             BOOLEAN NOT NULL DEFAULT TRUE,
    use_vibration         BOOLEAN NOT NULL DEFAULT TRUE
);


-- ============================================================
-- 5. CONTACTS
-- ============================================================

CREATE TABLE contacts_TEST (
    id              TEXT PRIMARY KEY,
    created         TIMESTAMPTZ NOT NULL,
    modified        TIMESTAMPTZ NOT NULL,
    user_id         UUID DEFAULT auth.uid(),

    name            TEXT NOT NULL,
    surname         TEXT NOT NULL,
    nickname        TEXT,
    mobile_phone    TEXT,
    email           TEXT,
    birthday        TEXT,
    group_name      TEXT NOT NULL,
    is_favorite     BOOLEAN NOT NULL DEFAULT FALSE,
    in_gift_list    BOOLEAN NOT NULL DEFAULT FALSE
);


-- ============================================================
-- 6. GIFT IDEAS
-- ============================================================

CREATE TABLE gift_ideas_TEST (
    id              TEXT PRIMARY KEY,
    created         TIMESTAMPTZ NOT NULL,
    modified        TIMESTAMPTZ NOT NULL,
    user_id         UUID DEFAULT auth.uid(),

    contact_id      TEXT NOT NULL REFERENCES contacts_TEST(id) ON DELETE CASCADE,
    name            TEXT NOT NULL,
    description     TEXT,
    link            TEXT,
    price           DOUBLE PRECISION,
    event           TEXT CHECK (event IN ('Christmas', 'Birthday', 'FathersDay', 'ValentinesDay', 'MothersDay', 'Anniversary', 'Other'))
);


-- ============================================================
-- 7. RECIPE BOOK
-- ============================================================

CREATE TABLE recipes_TEST (
    id                    TEXT PRIMARY KEY,
    created               TIMESTAMPTZ NOT NULL,
    modified              TIMESTAMPTZ NOT NULL,
    user_id               UUID DEFAULT auth.uid(),

    title                 TEXT NOT NULL,
    description           TEXT,
    ingredients           JSONB NOT NULL DEFAULT '[]',
    serving_size          TEXT,
    nutrition_per_serving JSONB NOT NULL DEFAULT '{}',
    instructions          JSONB NOT NULL DEFAULT '[]',
    meal_type             TEXT NOT NULL
);


-- ============================================================
-- 8. INGREDIENT STOCK
-- ============================================================

-- Metadata (Global reference or user-specific)
CREATE TABLE ingredient_metadata_TEST (
    id                  TEXT PRIMARY KEY,
    created             TIMESTAMPTZ NOT NULL,
    modified            TIMESTAMPTZ NOT NULL,
    user_id             UUID DEFAULT auth.uid(),

    name                TEXT NOT NULL,
    description         TEXT,
    food_group          TEXT NOT NULL,
    icon                TEXT,
    brand               TEXT,

    conversions         JSONB DEFAULT '[]',
    base_nutrition      JSONB DEFAULT '{}',
    nutrition_basis     JSONB DEFAULT '{}',
    preparation_methods JSONB DEFAULT '[]'
);

-- User-Specific Settings
CREATE TABLE user_stock_TEST (
    id                  TEXT PRIMARY KEY,
    created             TIMESTAMPTZ NOT NULL,
    modified            TIMESTAMPTZ NOT NULL,
    user_id             UUID DEFAULT auth.uid(),

    metadata_id         TEXT REFERENCES ingredient_metadata_TEST(id) ON DELETE CASCADE,
    storage_location    TEXT NOT NULL,
    primary_unit        TEXT NOT NULL,
    minimum_stock       DOUBLE PRECISION
);

-- Physical Batches
CREATE TABLE stock_batches_TEST (
    id                  TEXT PRIMARY KEY,
    created             TIMESTAMPTZ NOT NULL,
    modified            TIMESTAMPTZ NOT NULL,
    user_id             UUID DEFAULT auth.uid(),

    stock_id            TEXT REFERENCES user_stock_TEST(id) ON DELETE CASCADE,
    quantity            DOUBLE PRECISION NOT NULL DEFAULT 0,
    purchase_date       DATE NOT NULL,
    expiry_date         DATE
);

-- ============================================================
-- 9. TESTING SYSTEM
-- ============================================================

CREATE TABLE database_tests_TEST (
    id                  TEXT PRIMARY KEY,
    created             TIMESTAMPTZ NOT NULL,
    modified            TIMESTAMPTZ NOT NULL,
    user_id             UUID DEFAULT auth.uid(),

    plugin_id           TEXT NOT NULL,
    operation           TEXT NOT NULL,
    status              TEXT NOT NULL,
    error_message       TEXT
);


-- ============================================================
-- 9. PERMISSIONS
-- ============================================================

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO anon;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO authenticated;


-- ============================================================
-- 10. ROW LEVEL SECURITY (RLS)
-- ============================================================

ALTER TABLE calendar_TEST ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks_TEST ENABLE ROW LEVEL SECURITY;
ALTER TABLE alarms_TEST ENABLE ROW LEVEL SECURITY;
ALTER TABLE contacts_TEST ENABLE ROW LEVEL SECURITY;
ALTER TABLE gift_ideas_TEST ENABLE ROW LEVEL SECURITY;
ALTER TABLE recipes_TEST ENABLE ROW LEVEL SECURITY;
ALTER TABLE ingredient_metadata_TEST ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_stock_TEST ENABLE ROW LEVEL SECURITY;
ALTER TABLE stock_batches_TEST ENABLE ROW LEVEL SECURITY;
ALTER TABLE database_tests_TEST ENABLE ROW LEVEL SECURITY;

-- Dynamic Policies for User Isolation
CREATE POLICY "calendar_iso" ON calendar_TEST FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "tasks_iso" ON tasks_TEST FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "alarms_iso" ON alarms_TEST FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "contacts_iso" ON contacts_TEST FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "gift_ideas_iso" ON gift_ideas_TEST FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "recipes_iso" ON recipes_TEST FOR ALL USING (auth.uid() = user_id OR user_id IS NULL);
CREATE POLICY "meta_iso" ON ingredient_metadata_TEST FOR ALL USING (auth.uid() = user_id OR user_id IS NULL);
CREATE POLICY "stock_iso" ON user_stock_TEST FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "batch_iso" ON stock_batches_TEST FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "test_iso" ON database_tests_TEST FOR ALL USING (auth.uid() = user_id);


-- ============================================================
-- 11. EXAMPLE DATA
-- ============================================================

-- Metadata
INSERT INTO ingredient_metadata_TEST (id, created, modified, name, food_group)
VALUES ('meta_carrots', now(), now(), 'Carrots', 'VEGETABLES');

-- Recipe
INSERT INTO recipes_TEST (id, created, modified, title, meal_type)
VALUES ('recipe_001', now(), now(), 'Steamed Carrots', 'LUNCH');

-- Calendar
INSERT INTO calendar_TEST (id, created, modified, title, starting_date, is_priority)
VALUES ('cal_001', now(), now(), 'Dinner Party', CURRENT_DATE + 5, TRUE);

-- ============================================================
-- users (application profile table; mirrors core/Users/User.kt)
-- ============================================================
CREATE TABLE users_TEST (
    id          TEXT PRIMARY KEY,
    created     TIMESTAMPTZ NOT NULL,
    modified    TIMESTAMPTZ NOT NULL,
    name        TEXT NOT NULL DEFAULT '',
    email       TEXT NOT NULL DEFAULT '',
    user_id     UUID DEFAULT auth.uid()
);
ALTER TABLE users_TEST ENABLE ROW LEVEL SECURITY;
CREATE POLICY users_user_isolation ON users_TEST FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
