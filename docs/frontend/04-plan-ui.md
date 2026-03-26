# Plan UI (Tạo lộ trình)

## Điểm vào
- `frontend/lib/screens/goal_plan_screen.dart` (`GoalPlanScreen`)

## Input
- `goalType`: `"GIAM"` hoặc `"TANG"`
- `timeframeType`: `"DAYS" | "WEEKS" | "MONTHS" | "YEARS"`
- `timeframeValue`: số nguyên
- `targetWeightKg`: double
- truyền theo context:
  - `profileId` (có thể null)
  - `userId` (từ auth)

## Gọi API
- `POST /api/plan` thông qua `ApiClient.createPlan(GoalPlanRequest)`

## Render kết quả
- hiển thị `caloriesPerDay`
- danh sách ngày (`planJson.days`)
  - mỗi ngày là `ExpansionTile` chứa list món (`mealType`, `name`, `description`, `caloriesEstimated`)

## Lưu ý
- Nếu profile chưa tồn tại (`profileId == null`) backend vẫn cố resolve bằng `userId` (profile mới nhất), nhưng phụ thuộc DB đã có dữ liệu hay chưa.

