# Database (Postgres + Flyway)

Migrations nằm ở:
- `backend/src/main/resources/db/migration/V1__init.sql`

Flyway được bật trong `backend/src/main/resources/application.properties` (`spring.flyway.enabled=true`).

## Các bảng chính
### `app_user`
- `id` (BIGSERIAL PK)
- `email` (UNIQUE)
- `password_hash`
- `full_name`

### `nutrition_profile`
- `id` (PK)
- `user_id` -> `app_user(id)`
- `height_cm`, `weight_kg`, `age`, `gender`, `bmi`

### `weight_log`
- `id` (PK)
- `profile_id` -> `nutrition_profile(id)`
- `log_date`
- `weight_kg`

### `diet_plan`
- `id` (PK)
- `profile_id`
- `goal_type`, `target_weight_kg`
- `timeframe_type`, `timeframe_value`
- `start_date`, `end_date`
- `calories_per_day`
- `plan_json` (JSONB) : `MealPlanDto`
- `llm_raw_text` (TEXT | null)

### `meal_analysis`
- `id` (PK)
- `profile_id`
- `meal_name`
- `source_type` (`IMAGE`/`MANUAL`)
- `image_url`, `cloudinary_public_id` (tuỳ trường hợp)
- `total_calories`
- `analysis_json` (JSONB) : danh sách ingredient result

### `meal_ingredient`
- mỗi dòng là 1 ingredient trong 1 lần analysis
- `meal_analysis_id` -> `meal_analysis(id)`
- `ingredient_name`, `quantity_text`, `calories_estimated`

### `meal_substitution`
- lưu kết quả thay thế (manual)
- `profile_id`
- `original_meal_analysis_id` -> `meal_analysis(id)` (nullable)
- `substitute_meal_name`, `substitute_calories`
- `mode` (`MANUAL`)
- `is_acceptable`, `note`

### `calorie_log`
- `profile_id`
- `log_date`
- `calories_in`
- `meal_analysis_id` (nullable)
- `note`
- có ràng buộc `UNIQUE(profile_id, log_date)` -> backend `/api/calories` dùng upsert theo date

## Index (để query nhanh)
- `idx_weight_log_profile_date` trên `weight_log(profile_id, log_date)`
- `idx_diet_plan_profile_start_end` trên `diet_plan(profile_id, start_date, end_date)`
- `idx_meal_analysis_profile_analyzed` trên `meal_analysis(profile_id, analyzed_at desc)`
- `idx_calorie_log_profile_date` trên `calorie_log(profile_id, log_date)`

## Mapping endpoint -> bảng (quick)
- `/api/auth/*` -> `app_user`
- `/api/profile` -> `nutrition_profile`
- `/api/dashboard` -> `weight_log`, `calorie_log`, `diet_plan`
- `/api/meals/*` -> `meal_analysis`, `meal_ingredient`, `meal_substitution`
- `/api/weights` -> `weight_log`
- `/api/calories` -> `calorie_log`
- `/api/images/upload` -> không lưu DB (hiện tại chỉ trả URL về frontend)

