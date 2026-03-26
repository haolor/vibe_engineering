# ApiClient & Models (Flutter)

## Điểm vào
- `frontend/lib/api/api_client.dart`

## Base URL
- khởi tạo ở `main.dart`:
  - `static const String _baseUrl = 'http://localhost:8080';`

## Error handling
- `ApiClient._throwIfNotOk`:
  - nếu `statusCode` không phải 2xx -> ném `ApiException('API error ...')`

## Các method chính -> endpoint
- `register(RegisterRequest)` -> `POST /api/auth/register`
- `login(LoginRequest)` -> `POST /api/auth/login`
- `upsertProfile(ProfileRequest)` -> `POST /api/profile`
- `createPlan(GoalPlanRequest)` -> `POST /api/plan`
- `addWeight(WeightLogRequest)` -> `POST /api/weights`
- `addCalories(CalorieLogRequest)` -> `POST /api/calories`
- `getDashboard(profileId, from, to)` -> `GET /api/dashboard`
- `getBootstrap(userId)` -> `GET /api/bootstrap`
- `analyzeMeal(MealAnalyzeRequest)` -> `POST /api/meals/analyze`
- `autoSubstitute(AutoSubstituteRequest)` -> `POST /api/meals/substitute/auto`
- `manualSubstitute(ManualSubstituteRequest)` -> `POST /api/meals/substitute/manual`
- `uploadImage(XFile)` -> `POST /api/images/upload` (multipart field `file`)

## Date format
- `getDashboard`: chuyển `DateTime` thành `yyyy-MM-dd` để truyền query `from/to`
- `WeightLogRequest.logDate` và `CalorieLogRequest.logDate` cũng được format theo `yyyy-MM-dd` trước khi gửi.

