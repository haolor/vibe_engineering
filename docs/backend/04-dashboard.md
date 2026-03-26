# Dashboard (Biểu đồ 30 ngày)

## Endpoint
- `GET /api/dashboard?profileId={profileId?}&from={yyyy-MM-dd?}&to={yyyy-MM-dd?}`

### Default query
- `from`: nếu không truyền -> `today - 30 ngày`
- `to`: nếu không truyền -> `today`

## Response
`DashboardResponse`
- `profileId`: int
- `weightHistory`: danh sách `{ date: "yyyy-MM-dd", weightKg: number }`
- `plannedCaloriesHistory`: danh sách `{ date: "yyyy-MM-dd", caloriesPerDay: number }`

## Luồng xử lý (code)
- `DashboardController`:
  - parse query param `from/to` dạng `LocalDate`
  - gọi `DashboardService.getDashboard(profileId, fromEffective, toEffective)`
- `DashboardService.getDashboard`:
  1. Load `nutrition_profile`
  2. Query `weight_log` trong [from, to] -> `weightHistory`
  3. Query `calorie_log` trong [from, to] -> nếu có thì map `calories_in` -> `plannedCaloriesHistory`
  4. Nếu KHÔNG có calorie_log nhưng có `diet_plan` gần nhất:
     - fill `plannedCaloriesHistory` theo `diet_plan.caloriesPerDay` cho từng ngày nằm trong plan range giao với [from, to]

## Ghi chú
- Tên field `plannedCaloriesHistory` thực tế có thể là “calo đã log” (nếu người dùng đã log), hoặc “calo theo plan” (nếu chưa log).

