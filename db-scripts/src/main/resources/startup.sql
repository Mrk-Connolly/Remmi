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

DROP TABLE IF EXISTS calendar_groups CASCADE;
CREATE TABLE calendar_groups (
    id              TEXT PRIMARY KEY,
    created         TIMESTAMPTZ NOT NULL,
    modified        TIMESTAMPTZ NOT NULL,
    user_id         UUID DEFAULT auth.uid(),
    name            TEXT NOT NULL UNIQUE,
    color_hex       TEXT NOT NULL DEFAULT '#6200EE'
);

-- RLS
ALTER TABLE calendar ENABLE ROW LEVEL SECURITY;
ALTER TABLE calendar_groups ENABLE ROW LEVEL SECURITY;
CREATE POLICY "calendar_all" ON calendar FOR ALL TO anon USING (true) WITH CHECK (true);
CREATE POLICY "calendar_groups_all" ON calendar_groups FOR ALL TO anon USING (true) WITH CHECK (true);

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

-- RLS
ALTER TABLE contacts ENABLE ROW LEVEL SECURITY;
CREATE POLICY "contacts_all" ON contacts FOR ALL TO anon USING (true) WITH CHECK (true);

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

-- RLS
ALTER TABLE gift_ideas ENABLE ROW LEVEL SECURITY;
CREATE POLICY "gift_ideas_all" ON gift_ideas FOR ALL TO anon USING (true) WITH CHECK (true);

-- ============================================================
-- INGREDIENT STOCK PLUGIN SCHEMA
-- ============================================================

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

DROP TABLE IF EXISTS shops CASCADE;
CREATE TABLE shops (
    id              TEXT PRIMARY KEY,
    created         TIMESTAMPTZ NOT NULL,
    modified        TIMESTAMPTZ NOT NULL,
    user_id         UUID DEFAULT auth.uid(),
    name            TEXT NOT NULL,
    location        TEXT,
    source_plugin   TEXT,
    source_item_id  TEXT
);

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
    shop_id             TEXT REFERENCES shops(id) ON DELETE SET NULL,
    price               DOUBLE PRECISION,
    source_plugin       TEXT,
    source_item_id      TEXT
);

-- RLS
ALTER TABLE ingredient_metadata ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_stock ENABLE ROW LEVEL SECURITY;
ALTER TABLE shops ENABLE ROW LEVEL SECURITY;
ALTER TABLE stock_batches ENABLE ROW LEVEL SECURITY;
CREATE POLICY "metadata_all" ON ingredient_metadata FOR ALL TO anon USING (true) WITH CHECK (true);
CREATE POLICY "stock_all" ON user_stock FOR ALL TO anon USING (true) WITH CHECK (true);
CREATE POLICY "shops_all" ON shops FOR ALL TO anon USING (true) WITH CHECK (true);
CREATE POLICY "batches_all" ON stock_batches FOR ALL TO anon USING (true) WITH CHECK (true);

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
    ingredients           JSONB NOT NULL DEFAULT '[]', -- Legacy/Cache
    serving_size          TEXT,
    nutrition_per_serving JSONB NOT NULL DEFAULT '{}',
    instructions          JSONB NOT NULL DEFAULT '[]',
    meal_type             TEXT NOT NULL,
    source_plugin         TEXT,
    source_item_id        TEXT
);

-- RLS
ALTER TABLE recipes ENABLE ROW LEVEL SECURITY;
CREATE POLICY "recipes_all" ON recipes FOR ALL TO anon USING (true) WITH CHECK (true);

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

-- RLS
ALTER TABLE saved_locations ENABLE ROW LEVEL SECURITY;
CREATE POLICY "saved_locations_all" ON saved_locations FOR ALL TO anon USING (true) WITH CHECK (true);

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
    completed_at    TIMESTAMPTZ,
    due_date        TIMESTAMPTZ,
    is_priority     BOOLEAN NOT NULL DEFAULT FALSE,
    group_name      TEXT,
    subgroup        TEXT,
    parent_task     TEXT,
    repeat          JSONB,
    reminders       TEXT[] NOT NULL DEFAULT '{}',
    relationships   TEXT[] NOT NULL DEFAULT '{}',
    create_calendar BOOLEAN NOT NULL DEFAULT FALSE,
    create_alarm    BOOLEAN NOT NULL DEFAULT FALSE,
    source_plugin   TEXT,
    source_item_id  TEXT
);

-- RLS
ALTER TABLE tasks ENABLE ROW LEVEL SECURITY;
CREATE POLICY "tasks_all" ON tasks FOR ALL TO anon USING (true) WITH CHECK (true);

-- ============================================================
-- CALL RECORDER PLUGIN SCHEMA
-- ============================================================

DROP TABLE IF EXISTS call_recordings CASCADE;
CREATE TABLE call_recordings (
    id              TEXT PRIMARY KEY,
    created         TIMESTAMPTZ NOT NULL,
    modified        TIMESTAMPTZ NOT NULL,
    user_id         UUID DEFAULT auth.uid(),
    file_path       TEXT NOT NULL,
    duration_millis BIGINT NOT NULL DEFAULT 0,
    direction       TEXT NOT NULL,
    phone_number    TEXT,
    contact_name    TEXT,
    status          TEXT NOT NULL DEFAULT 'COMPLETED',
    error_message   TEXT,
    group_name      TEXT,
    app_package     TEXT,
    source_plugin   TEXT DEFAULT 'call_recorder',
    source_item_id  TEXT
);

-- RLS
ALTER TABLE call_recordings ENABLE ROW LEVEL SECURITY;
CREATE POLICY "call_recordings_all" ON call_recordings FOR ALL TO anon USING (true) WITH CHECK (true);

-- ============================================================
-- PERMISSIONS
-- ============================================================

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO anon;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO authenticated;
