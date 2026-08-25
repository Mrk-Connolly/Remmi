-- ============================================================
-- NUTRITION SYSTEM MIGRATION
-- ============================================================

-- 1. Update ingredient_metadata
ALTER TABLE ingredient_metadata
ADD COLUMN IF NOT EXISTS calories_per_100g DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS proteins_per_100g DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS carbs_per_100g DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS sugar_per_100g DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS fats_per_100g DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS fiber_per_100g DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS sodium_per_100g DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS additional_nutrients JSONB DEFAULT '[]',
ADD COLUMN IF NOT EXISTS estimated_shelf_life_min_days INTEGER,
ADD COLUMN IF NOT EXISTS estimated_shelf_life_max_days INTEGER;

-- 2. Update recipes
ALTER TABLE recipes
ADD COLUMN IF NOT EXISTS calories_per_serving DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS proteins_per_serving DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS carbs_per_serving DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS sugar_per_serving DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS fats_per_serving DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS fiber_per_serving DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS sodium_per_serving DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS additional_nutrients_per_serving JSONB DEFAULT '[]';

-- Note: base_nutrition (JSONB) in ingredient_metadata and
-- nutrition_per_serving (JSONB) in recipes can be kept for backward compatibility
-- or migrated to the new columns using a separate UPDATE script if desired.
