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
    time                  TIMESTAMPTZ NOT NULL,
    repeatable            TEXT[] NOT NULL DEFAULT '{}',
    custom                TEXT[] NOT NULL DEFAULT '{}',
    use_sound             BOOLEAN NOT NULL DEFAULT TRUE,
    use_vibration         BOOLEAN NOT NULL DEFAULT TRUE,
    source_plugin         TEXT,
    source_item_id        TEXT
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE alarms TO anon;

ALTER TABLE alarms ENABLE ROW LEVEL SECURITY;

CREATE POLICY "alarms_all" ON alarms FOR ALL TO anon USING (true) WITH CHECK (true);

-- EXAMPLES
INSERT INTO alarms (id, created, modified, title, is_priority, time) VALUES
('alarm_ex_1', now(), now(), 'Morning Alarm', FALSE, now() + interval '1 day'),
('alarm_ex_2', now(), now(), 'Gym Reminder', TRUE, now() + interval '18 hours'),
('alarm_ex_3', now(), now(), 'Medication', TRUE, now() + interval '2 hours');
-- ============================================================
-- CALENDAR PLUGIN SCHEMA
-- ============================================================

DROP TABLE IF EXISTS calendar CASCADE;

CREATE TABLE calendar (
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
    linked_alarm    TEXT,
    is_repeatable   BOOLEAN NOT NULL DEFAULT FALSE,
    repeatable_type TEXT,
    create_alarm    BOOLEAN NOT NULL DEFAULT FALSE,
    create_task     BOOLEAN NOT NULL DEFAULT FALSE,
    create_location BOOLEAN NOT NULL DEFAULT FALSE,
    create_contact  BOOLEAN NOT NULL DEFAULT FALSE,
    source_plugin   TEXT,
    source_item_id  TEXT
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE calendar TO anon;

ALTER TABLE calendar ENABLE ROW LEVEL SECURITY;

CREATE POLICY "calendar_all" ON calendar FOR ALL TO anon USING (true) WITH CHECK (true);

-- EXAMPLES
INSERT INTO calendar (id, created, modified, title, starting_date, is_priority, group_name) VALUES
('cal_ex_1', now(), now(), 'Work Meeting', CURRENT_DATE + 1, FALSE, 'Work'),
('cal_ex_2', now(), now(), 'Doctor Appointment', CURRENT_DATE + 2, TRUE, 'Personal'),
('cal_ex_3', now(), now(), 'Birthday Party', CURRENT_DATE + 5, FALSE, 'Social');

-- ============================================================
-- CALENDAR GROUPS
-- ============================================================

DROP TABLE IF EXISTS calendar_groups CASCADE;

CREATE TABLE calendar_groups (
    id              TEXT PRIMARY KEY,
    created         TIMESTAMPTZ NOT NULL,
    modified        TIMESTAMPTZ NOT NULL,
    user_id         UUID DEFAULT auth.uid(),
    name            TEXT NOT NULL UNIQUE,
    color_hex       TEXT NOT NULL DEFAULT '#6200EE'
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE calendar_groups TO anon;
ALTER TABLE calendar_groups ENABLE ROW LEVEL SECURITY;
CREATE POLICY "calendar_groups_all" ON calendar_groups FOR ALL TO anon USING (true) WITH CHECK (true);

INSERT INTO calendar_groups (id, created, modified, name, color_hex) VALUES
('cg_1', now(), now(), 'Work', '#2196F3'),
('cg_2', now(), now(), 'Personal', '#E91E63'),
('cg_3', now(), now(), 'Social', '#FF9800');
-- ============================================================
-- CONTACTS PLUGIN SCHEMA
-- ============================================================

DROP TABLE IF EXISTS contacts CASCADE;

CREATE TABLE contacts (
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
    in_gift_list    BOOLEAN NOT NULL DEFAULT FALSE,
    source_plugin   TEXT,
    source_item_id  TEXT
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE contacts TO anon;

ALTER TABLE contacts ENABLE ROW LEVEL SECURITY;

CREATE POLICY "contacts_all" ON contacts FOR ALL TO anon USING (true) WITH CHECK (true);

-- EXAMPLES
INSERT INTO contacts (id, created, modified, name, surname, group_name, is_favorite, in_gift_list) VALUES
('contact_ex_1', now(), now(), 'John', 'Doe', 'General', true, true),
('contact_ex_2', now(), now(), 'Jane', 'Smith', 'Family', true, true),
('contact_ex_3', now(), now(), 'Bob', 'Wilson', 'Friends', false, true);
-- ============================================================
-- GIFT IDEAS PLUGIN SCHEMA
-- ============================================================

DROP TABLE IF EXISTS gift_ideas CASCADE;

CREATE TABLE gift_ideas (
    id              TEXT PRIMARY KEY,
    created         TIMESTAMPTZ NOT NULL,
    modified        TIMESTAMPTZ NOT NULL,
    user_id         UUID DEFAULT auth.uid(),
    contact_id      TEXT NOT NULL REFERENCES contacts(id) ON DELETE CASCADE,
    name            TEXT NOT NULL,
    description     TEXT,
    link            TEXT,
    price           DOUBLE PRECISION,
    event           TEXT CHECK (event IN ('Christmas', 'Birthday', 'FathersDay', 'ValentinesDay', 'MothersDay', 'Anniversary', 'Other')),
    source_plugin   TEXT,
    source_item_id  TEXT
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE gift_ideas TO anon;

ALTER TABLE gift_ideas ENABLE ROW LEVEL SECURITY;

CREATE POLICY "gift_ideas_all" ON gift_ideas FOR ALL TO anon USING (true) WITH CHECK (true);

-- EXAMPLES
INSERT INTO gift_ideas (id, created, modified, contact_id, name, event) VALUES
('gift_ex_1', now(), now(), 'contact_ex_1', 'Wireless Headphones', 'Birthday'),
('gift_ex_2', now(), now(), 'contact_ex_2', 'Coffee Maker', 'Christmas'),
('gift_ex_3', now(), now(), 'contact_ex_3', 'Gift Card', 'Anniversary');
-- ============================================================
-- INGREDIENT STOCK PLUGIN SCHEMA
-- ============================================================

-- 1. Metadata Table (Global or User-specific)
DROP TABLE IF EXISTS ingredient_metadata CASCADE;
CREATE TABLE ingredient_metadata (
    id                  TEXT PRIMARY KEY,
    created             TIMESTAMPTZ NOT NULL,
    modified            TIMESTAMPTZ NOT NULL,
    user_id             UUID DEFAULT auth.uid(),

    name                TEXT NOT NULL,
    description         TEXT,
    food_group          TEXT NOT NULL,
    icon                TEXT,
    brand               TEXT,

    allowed_units       JSONB DEFAULT '[]',
    conversions         JSONB DEFAULT '[]',
    base_nutrition      JSONB DEFAULT '{}',
    nutrition_basis     JSONB DEFAULT NULL,
    preparation_methods JSONB DEFAULT '[]',

    estimated_shelf_life_min_days INTEGER,
    estimated_shelf_life_max_days INTEGER,

    source_plugin       TEXT,
    source_item_id      TEXT
);

-- 2. User Stock Table (Linking user to metadata with specific settings)
DROP TABLE IF EXISTS user_stock CASCADE;
CREATE TABLE user_stock (
    id                  TEXT PRIMARY KEY,
    created             TIMESTAMPTZ NOT NULL,
    modified            TIMESTAMPTZ NOT NULL,
    user_id             UUID DEFAULT auth.uid(),

    metadata_id         TEXT REFERENCES ingredient_metadata(id) ON DELETE CASCADE,
    storage_location    TEXT NOT NULL,
    primary_unit        TEXT NOT NULL,
    minimum_stock       DOUBLE PRECISION,

    source_plugin       TEXT,
    source_item_id      TEXT
);

-- 3. Batches Table (Physical stock with expiry dates)
DROP TABLE IF EXISTS stock_batches CASCADE;
CREATE TABLE stock_batches (
    id                  TEXT PRIMARY KEY,
    created             TIMESTAMPTZ NOT NULL,
    modified            TIMESTAMPTZ NOT NULL,
    user_id             UUID DEFAULT auth.uid(),

    stock_id            TEXT REFERENCES user_stock(id) ON DELETE CASCADE,
    quantity            DOUBLE PRECISION NOT NULL DEFAULT 0,
    purchase_date       DATE NOT NULL,
    expiry_date         DATE,

    source_plugin       TEXT,
    source_item_id      TEXT
);

-- PERMISSIONS
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE ingredient_metadata TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE user_stock TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE stock_batches TO authenticated;

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE ingredient_metadata TO anon;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE user_stock TO anon;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE stock_batches TO anon;

-- RLS POLICIES
ALTER TABLE ingredient_metadata ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_stock ENABLE ROW LEVEL SECURITY;
ALTER TABLE stock_batches ENABLE ROW LEVEL SECURITY;

CREATE POLICY "metadata_isolation" ON ingredient_metadata FOR ALL USING (auth.uid() = user_id OR user_id IS NULL) WITH CHECK (auth.uid() = user_id OR user_id IS NULL);
CREATE POLICY "stock_isolation" ON user_stock FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY "batches_isolation" ON stock_batches FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- EXAMPLES
INSERT INTO ingredient_metadata (id, created, modified, name, food_group) VALUES
('meta_ex_carrots', now(), now(), 'Carrots', 'VEGETABLES'),
('meta_ex_apples', now(), now(), 'Apples', 'FRUIT'),
('meta_ex_milk', now(), now(), 'Milk', 'DAIRY');

INSERT INTO user_stock (id, created, modified, metadata_id, storage_location, primary_unit) VALUES
('stock_ex_carrots', now(), now(), 'meta_ex_carrots', 'PANTRY', 'GRAMS'),
('stock_ex_apples', now(), now(), 'meta_ex_apples', 'FRIDGE', 'UNITS'),
('stock_ex_milk', now(), now(), 'meta_ex_milk', 'FRIDGE', 'LITERS');

INSERT INTO stock_batches (id, created, modified, stock_id, quantity, purchase_date, expiry_date) VALUES
('batch_ex_carrots_1', now(), now(), 'stock_ex_carrots', 1000.0, current_date, current_date + interval '14 days'),
('batch_ex_apples_1', now(), now(), 'stock_ex_apples', 6.0, current_date, current_date + interval '7 days'),
('batch_ex_milk_1', now(), now(), 'stock_ex_milk', 2.0, current_date, current_date + interval '5 days');
-- ============================================================
-- RECIPE BOOK PLUGIN SCHEMA
-- ============================================================

DROP TABLE IF EXISTS recipes CASCADE;

CREATE TABLE recipes (
    id                    TEXT PRIMARY KEY,
    created               TIMESTAMPTZ NOT NULL,
    modified              TIMESTAMPTZ NOT NULL,
    user_id               UUID DEFAULT auth.uid(),

    title                 TEXT NOT NULL,
    description           TEXT,
    image_path            TEXT,
    servings              INTEGER NOT NULL DEFAULT 1,
    prep_time             INTEGER NOT NULL DEFAULT 0,
    cooking_time          INTEGER NOT NULL DEFAULT 0,
    oven_time             INTEGER NOT NULL DEFAULT 0,
    resting_time          INTEGER NOT NULL DEFAULT 0,
    total_ingredient_ids  JSONB NOT NULL DEFAULT '[]',
    steps                 JSONB NOT NULL DEFAULT '[]',

    ingredients           JSONB NOT NULL DEFAULT '[]',
    serving_size          TEXT,
    nutrition_per_serving JSONB NOT NULL DEFAULT '{}',
    instructions          JSONB NOT NULL DEFAULT '[]',
    meal_type             TEXT NOT NULL,
    source_plugin         TEXT,
    source_item_id        TEXT
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE recipes TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE recipes TO anon;

ALTER TABLE recipes ENABLE ROW LEVEL SECURITY;

-- Only show recipes belonging to the user
CREATE POLICY "recipes_user_isolation" ON recipes
    FOR ALL
    USING (auth.uid() = user_id OR user_id IS NULL)
    WITH CHECK (auth.uid() = user_id OR user_id IS NULL);

-- EXAMPLES
INSERT INTO recipes (id, created, modified, title, description, meal_type) VALUES
('recipe_ex_1', now(), now(), 'Spaghetti Carbonara', 'Classic Italian pasta dish', 'LUNCH'),
('recipe_ex_2', now(), now(), 'Chicken Salad', 'Healthy and fresh salad', 'LUNCH'),
('recipe_ex_3', now(), now(), 'Banana Bread', 'Sweet and moist bread', 'SNACK');
-- ============================================================
-- MAPS PLUGIN SCHEMA
-- ============================================================

DROP TABLE IF EXISTS saved_locations CASCADE;

CREATE TABLE saved_locations (
    id              TEXT PRIMARY KEY,
    created         TIMESTAMPTZ NOT NULL,
    modified        TIMESTAMPTZ NOT NULL,
    user_id         UUID DEFAULT auth.uid(),

    name            TEXT NOT NULL,
    address         TEXT,
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION,
    category        TEXT DEFAULT 'General',
    linked_calendar_event TEXT,
    source_plugin   TEXT,
    source_item_id  TEXT
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE saved_locations TO anon;
ALTER TABLE saved_locations ENABLE ROW LEVEL SECURITY;
CREATE POLICY "saved_locations_all" ON saved_locations FOR ALL TO anon USING (true) WITH CHECK (true);

-- EXAMPLES
INSERT INTO saved_locations (id, created, modified, name, address, category) VALUES
('loc_ex_1', now(), now(), 'Home', '123 Main St', 'Personal'),
('loc_ex_2', now(), now(), 'Work', '456 Business Ave', 'Work');

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
    completed             BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at          TIMESTAMPTZ,
    due_date              TIMESTAMPTZ,
    is_priority           BOOLEAN NOT NULL DEFAULT FALSE,
    group_name            TEXT,
    subgroup              TEXT,
    parent_task           TEXT,
    repeat                JSONB,
    reminders       TEXT[] NOT NULL DEFAULT '{}',
    relationships   TEXT[] NOT NULL DEFAULT '{}',
    create_calendar BOOLEAN NOT NULL DEFAULT FALSE,
    create_alarm    BOOLEAN NOT NULL DEFAULT FALSE,
    source_plugin   TEXT,
    source_item_id  TEXT
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE tasks TO anon;

ALTER TABLE tasks ENABLE ROW LEVEL SECURITY;

CREATE POLICY "tasks_all" ON tasks FOR ALL TO anon USING (true) WITH CHECK (true);

-- EXAMPLES
INSERT INTO tasks (id, created, modified, title, is_priority, group_name) VALUES
('task_ex_1', now(), now(), 'Buy Groceries', TRUE, 'Personal'),
('task_ex_2', now(), now(), 'Clean House', FALSE, 'Home'),
('task_ex_3', now(), now(), 'Write Report', TRUE, 'Work');
