# Profile (Upsert BMI) & Bootstrap

## Endpoint
- `POST /api/profile` (upsert profile/BMI)
- `GET /api/bootstrap?userId={userId}` (lấy profile/plan mới nhất để frontend bootstrap)

## Request/Response
### Upsert profile
`ProfileRequest` (JSON)
- `userId`: int (bắt buộc)
- `heightCm`: double | null
- `heightFt`: double | null
- `weightKg`: double | null
- `weightLbs`: double | null
- `age`: int (bắt buộc, phải > 0)
- `gender`: string `"Nam"`/`"Nữ"` (backend normalize)

Trả về: `ProfileResponse`
- `profileId`: int
- `heightCm`: double
- `weightKg`: double
- `age`: int
- `gender`: string `"Nam"`/`"Nữ"` (hoặc giữ nguyên nếu input lạ)
- `bmi`: double

### Bootstrap
`userId` query param.

Trả về: `BootstrapResponse`
- `latestProfile`: `ProfileResponse` | null
- `latestPlan`: `PlanResponse` | null

## Luồng xử lý (code)
- `ProfileController`:
  - `POST /api/profile` -> `NutritionService.upsertProfile`
  - `GET /api/bootstrap` -> `NutritionService.getBootstrap`
- `NutritionService.upsertProfile`:
  - Convert `heightCm`/`heightFt` -> cm, `weightKg`/`weightLbs` -> kg
  - Tính BMI bằng công thức chuẩn `weightKg/(h^2)`
  - Save/update entity `nutrition_profile`

## Lưu ý
- Nếu chưa có profile cho `userId` thì `latestProfile` sẽ là `null`.

