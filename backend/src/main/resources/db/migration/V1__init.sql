-- Unified initial schema: auth, profile, meals/calories, dashboard support.

CREATE TABLE IF NOT EXISTS app_user (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS nutrition_profile (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES app_user(id) ON DELETE SET NULL,
    height_cm DOUBLE PRECISION NOT NULL,
    weight_kg DOUBLE PRECISION NOT NULL,
    age INTEGER NOT NULL,
    gender VARCHAR(10) NOT NULL,
    bmi DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS weight_log (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES nutrition_profile(id) ON DELETE CASCADE,
    log_date DATE NOT NULL,
    weight_kg DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS diet_plan (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES nutrition_profile(id) ON DELETE CASCADE,
    goal_type VARCHAR(10) NOT NULL,
    target_weight_kg DOUBLE PRECISION NOT NULL,
    timeframe_type VARCHAR(10) NOT NULL,
    timeframe_value INTEGER NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    calories_per_day DOUBLE PRECISION NOT NULL,
    plan_json JSONB NOT NULL,
    llm_raw_text TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS meal_analysis (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES nutrition_profile(id) ON DELETE CASCADE,
    meal_name VARCHAR(255) NOT NULL,
    source_type VARCHAR(20) NOT NULL, -- IMAGE or MANUAL
    image_url TEXT,
    cloudinary_public_id VARCHAR(255),
    total_calories DOUBLE PRECISION NOT NULL,
    analysis_json JSONB,
    analyzed_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS meal_ingredient (
    id BIGSERIAL PRIMARY KEY,
    meal_analysis_id BIGINT NOT NULL REFERENCES meal_analysis(id) ON DELETE CASCADE,
    ingredient_name VARCHAR(255) NOT NULL,
    quantity_text VARCHAR(100),
    calories_estimated DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS meal_substitution (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES nutrition_profile(id) ON DELETE CASCADE,
    original_meal_analysis_id BIGINT REFERENCES meal_analysis(id) ON DELETE SET NULL,
    substitute_meal_name VARCHAR(255) NOT NULL,
    substitute_calories DOUBLE PRECISION NOT NULL,
    mode VARCHAR(20) NOT NULL, -- AUTO or MANUAL
    is_acceptable BOOLEAN NOT NULL,
    note TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS calorie_log (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES nutrition_profile(id) ON DELETE CASCADE,
    log_date DATE NOT NULL,
    calories_in DOUBLE PRECISION NOT NULL,
    meal_analysis_id BIGINT REFERENCES meal_analysis(id) ON DELETE SET NULL,
    note TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    UNIQUE(profile_id, log_date)
);

CREATE INDEX IF NOT EXISTS idx_weight_log_profile_date
    ON weight_log(profile_id, log_date);

CREATE INDEX IF NOT EXISTS idx_diet_plan_profile_start_end
    ON diet_plan(profile_id, start_date, end_date);

CREATE INDEX IF NOT EXISTS idx_meal_analysis_profile_analyzed
    ON meal_analysis(profile_id, analyzed_at DESC);

CREATE INDEX IF NOT EXISTS idx_calorie_log_profile_date
    ON calorie_log(profile_id, log_date);

CREATE INDEX IF NOT EXISTS idx_nutrition_profile_user_created
    ON nutrition_profile(user_id, created_at DESC);
