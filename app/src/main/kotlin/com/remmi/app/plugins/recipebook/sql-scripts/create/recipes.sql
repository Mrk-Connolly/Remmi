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
    meal_type             TEXT NOT NULL
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE recipes TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE recipes TO anon;

ALTER TABLE recipes ENABLE ROW LEVEL SECURITY;

-- Only show recipes belonging to the user
CREATE POLICY "recipes_user_isolation" ON recipes
    FOR ALL
    USING (auth.uid() = user_id OR user_id IS NULL)
    WITH CHECK (auth.uid() = user_id OR user_id IS NULL);

-- EXAMPLE
INSERT INTO recipes (id, created, modified, title, description, meal_type)
VALUES ('recipe_ex_1', now(), now(), 'Spaghetti Carbonara', 'Classic Italian pasta dish', 'LUNCH');
