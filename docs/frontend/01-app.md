# Frontend App Entry (main.dart)

## Điểm vào
- `frontend/lib/main.dart`

## Luồng auth -> bootstrap
1. `MyApp` tạo `MyHomePage`
2. `MyHomePage` tạo `ApiClient(baseUrl: http://localhost:8080)`
3. Nếu `._auth == null` -> hiển thị `AuthScreen`
4. Khi login/register thành công:
   - `_bootstrapAfterLogin(auth)`
   - gọi `GET /api/bootstrap?userId={userId}`
   - lấy:
     - `latestProfile` -> `profileId`
     - `latestPlan` -> `currentPlan`

## Điều hướng
`BottomNavigationBar` (3 tab):
- `ProfileScreen` (tab Profile)
- `GoalPlanScreen` (tab Plan)
- `DashboardScreen` (tab Dashboard)

Logout:
- reset `_auth`, `_latestProfile`, `_profileId`, `_currentPlan`, `_tabIndex`

## Lưu ý baseUrl
- Trong `main.dart`, base URL đang là `http://localhost:8080`.
- Nếu chạy trên Android emulator có thể cần đổi theo network host (ví dụ `http://10.0.2.2:8080`).

