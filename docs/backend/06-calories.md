# Calories Log

## Endpoint
- `POST /api/calories`

## Request
`CalorieLogRequest` (JSON)
- `profileId`: int | null
- `caloriesIn`: double (bắt buộc)
- `logDate`: ISO date string | null (nếu null -> `today`)
- `mealAnalysisId`: int | null (liên kết tới kết quả phân tích món)
- `note`: string | null

## Response
`CalorieLogResponse`
- `logId`: int
- `logDate`: ISO date string
- `caloriesIn`: double

## Luồng xử lý
- `CaloriesController` -> `CalorieService.addOrUpdate`
- Service:
  - resolve `NutritionProfile` theo `profileId` hoặc lấy profile mới nhất
  - find log theo `(profileId, logDate)`:
    - có -> update
    - không -> tạo mới
- lưu `calorie_log`

