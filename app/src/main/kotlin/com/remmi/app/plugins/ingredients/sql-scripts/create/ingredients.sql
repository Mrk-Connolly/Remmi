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

    conversions         JSONB DEFAULT '[]',
    base_nutrition      JSONB DEFAULT '{}',
    nutrition_basis     JSONB DEFAULT NULL,
    preparation_methods JSONB DEFAULT '[]'
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
    minimum_stock       DOUBLE PRECISION
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
    expiry_date         DATE
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
INSERT INTO ingredient_metadata (id, created, modified, name, food_group)
VALUES ('meta_ex_carrots', now(), now(), 'Carrots', 'VEGETABLES');

INSERT INTO user_stock (id, created, modified, metadata_id, storage_location, primary_unit)
VALUES ('stock_ex_carrots', now(), now(), 'meta_ex_carrots', 'PANTRY', 'GRAMS');

INSERT INTO stock_batches (id, created, modified, stock_id, quantity, purchase_date, expiry_date)
VALUES ('batch_ex_carrots_1', now(), now(), 'stock_ex_carrots', 1000.0, current_date, current_date + interval '14 days');
