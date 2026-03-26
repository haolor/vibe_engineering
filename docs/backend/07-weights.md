# Weight Log

## Endpoint
- `POST /api/weights`

## Request
`WeightLogRequest` (JSON)
- `profileId`: int | null
- `weightKg`: double | null
- `weightLbs`: double | null
- `logDate`: ISO date string | null (nếu null -> `today`)

## Response
`WeightLogResponse`
- `logId`: int
- `logDate`: ISO date string
- `weightKg`: double

## Luồng xử lý
- `WeightsController` -> `WeightService.addWeightLog`
- Service:
  - resolve profile theo `profileId` hoặc profile mới nhất
  - convert `weightLbs` -> kg nếu cần (backend dùng `BasicNutritionCalculator`)
  - tạo `weight_log` mới và lưu DB

