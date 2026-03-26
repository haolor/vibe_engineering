# Plan (Tạo lộ trình ăn uống)

## Endpoint
- `POST /api/plan`

## Request/Response
### GoalPlanRequest (JSON)
- `goalType`: string `"GIAM"`/`"TANG"` (backend dùng nội dung để suy ra giảm/tăng)
- `targetWeightKg`: double (bắt buộc, phải > 0)
- `timeframeType`: string `"DAYS"`/`"WEEKS"`/`"MONTHS"`/`"YEARS"`
- `timeframeValue`: int (bắt buộc, > 0)
- `profileId`: int | null (tuỳ chọn)
- `userId`: int | null (tuỳ chọn)

### PlanResponse (JSON)
- `planId`: int
- `profileId`: int
- `goalType`: string | null
- `targetWeightKg`: double | null
- `startDate`: ISO date string
- `endDate`: ISO date string
- `caloriesPerDay`: double
- `planJson`: `MealPlanDto`

## Luồng xử lý (code)
- `PlanController` -> `NutritionService.createPlan`
- `NutritionService.createPlan`:
  1. Resolve profile:
     - nếu có `profileId` -> lấy theo `profileId`
     - else nếu có `userId` -> lấy profile mới nhất của user
     - else -> lấy profile mới nhất toàn hệ thống
  2. Tính `caloriesPerDay`:
     - dùng `BasicNutritionCalculator.calculateCaloriesPerDay`:
       - Mifflin-St Jeor + activity factor cố định `1.2`
       - goal "TANG" => `maintenance + 300`
       - goal "GIAM" => `maintenance - 500` (có floor min `1200`)
  3. Tính timeframe:
     - `timeframeDays` quy đổi từ type/value
     - `start = today`, `end = start + numberOfDays - 1`
  4. Sinh `MealPlanDto`:
     - nếu Gemini được cấu hình (`gemini.api.key` khác rỗng):
       - gọi `GeminiMealPlanService.generateMealPlan`
       - cố parse JSON (Gemini có thể trả kèm text -> backend sẽ “extract” object JSON đầu tiên)
       - nếu parse/gọi LLM fail -> fallback sang heuristic
     - nếu Gemini chưa cấu hình hoặc fallback -> `MealPlanHeuristicService.generatePlan`
  5. Lưu vào DB:
     - `diet_plan.plan_json` (JSONB) + `llm_raw_text` (có thể null)

## Heuristic fallback (nếu Gemini không có)
- `MealPlanHeuristicService` chia calories theo tỉ lệ:
  - SANG: ~25%
  - TRUA: ~35%
  - TOI: phần còn lại
- Tên món trong mỗi bữa được “cycle” theo danh sách cố định.

