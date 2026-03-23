-- Initial schema for MVP: profile, weight logs, and diet plans.

CREATE TABLE IF NOT EXISTS nutrition_profile (
    id BIGSERIAL PRIMARY KEY,
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
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    UNIQUE(profile_id, log_date)
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

CREATE INDEX IF NOT EXISTS idx_weight_log_profile_date ON weight_log(profile_id, log_date);
CREATE INDEX IF NOT EXISTS idx_diet_plan_profile_start_end ON diet_plan(profile_id, start_date, end_date);

