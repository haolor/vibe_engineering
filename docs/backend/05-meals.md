# Meals: Phân tích món & Gợi ý thay thế

## Endpoint
- `POST /api/meals/analyze`
- `POST /api/meals/substitute/auto`
- `POST /api/meals/substitute/manual`

## 1) Phân tích món ăn
### `POST /api/meals/analyze`
`MealAnalyzeRequest` (JSON)
- `profileId`: int | null
- `mealName`: string
- `imageUrl`: string | null (backend dùng để set `sourceType`)
- `ingredients`: array
  - mỗi item:
    - `name`: string
    - `quantityText`: string | null (display)
    - `amount`: double | null (mặc định ~1 nếu null/<=0)

Trả về: `MealAnalyzeResponse`
- `mealAnalysisId`: int
- `mealName`: string
- `totalCalories`: double
- `ingredients`: list `{ name, quantityText, caloriesEstimated }`

### Cách estimate calories (MVP)
- `MealService.CALORIES_CATALOG`: mapping keyword -> calories base (kcal/1 phần)
- Backend convert tên ingredient -> lowercase và `contains` keyword đầu tiên match được
- `total = base * amount`
- Nếu không có ingredient hợp lệ:
  - tạo 1 ingredient fallback với `mealName`
  - `quantityText = "image-detected"` nếu có `imageUrl`, ngược lại `"1 phan"`

### Lưu DB
- Save `meal_analysis`
- Save từng `meal_ingredient` (1 dòng/ingredient result)

## 2) Thay thế tự động
### `POST /api/meals/substitute/auto`
`AutoSubstituteRequest` (JSON)
- `profileId`: int | null
- `originalCalories`: double | null
- `originalMealAnalysisId`: int | null

Quy tắc xác định calories ban đầu:
- dùng `originalCalories` nếu > 0
- nếu không có -> lấy `MealAnalysis.totalCalories` theo `originalMealAnalysisId`
- nếu cả hai đều thiếu -> lỗi `IllegalArgumentException`

Trả về: `AutoSubstituteResponse`
- `originalCalories`: double
- `options`: tối đa 5 items, sort theo `deltaCalories` tăng dần

## 3) Thay thế manual (kiểm tra chênh lệch)
### `POST /api/meals/substitute/manual`
`ManualSubstituteRequest` (JSON)
- `profileId`: int | null
- `originalCalories`: double | null
- `originalMealAnalysisId`: int | null
- `substituteMealName`: string
- `ingredients`: array `{ name, quantityText, amount }`

Quy tắc accept/reject:
- `diff = abs(substituteCalories - originalCalories)`
- `acceptable = diff <= max(80, originalCalories * 0.15)`
- `note`:
  - acceptable -> `"Co the thay the mon goi y"`
  - không -> `"Chenh lech calo cao, can can nhac"`

### Lưu DB
- Save `meal_substitution` với `mode = "MANUAL"`

