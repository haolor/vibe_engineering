# Vibe Engineering - Tài liệu “vibe lại” nhanh

Project gồm 2 phần:
- `backend/`: Spring Boot (Java) cung cấp API REST + lưu dữ liệu Postgres (Flyway migrations).
- `frontend/`: Flutter (Dart) gọi API thông qua `ApiClient` và hiển thị các màn hình.

## Kiến trúc tổng
Flutter -> gọi các endpoint trong Spring Boot:
- Auth: `/api/auth/*`
- Profile/BMI + bootstrap: `/api/profile`, `/api/bootstrap`
- Tạo lộ trình: `/api/plan`
- Dashboard: `/api/dashboard`
- Phân tích món & gợi ý thay thế: `/api/meals/*`
- Lưu cân nặng & calo: `/api/weights`, `/api/calories`
- Upload ảnh món ăn: `/api/images/upload`

## Chạy dev
Script `run-dev` sẽ tự:
1. Start backend bằng `gradlew.bat bootRun`
2. Start Flutter `flutter run` trên thiết bị bạn chỉ định

Chạy (PowerShell, Windows):
```powershell
.\run-dev.ps1 chrome
```

## Cấu hình quan trọng (không commit secrets)
Backend đọc biến môi trường từ `backend/.env`:
- DB: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- Gemini (tuỳ chọn): `GEMINI_AI_KEY`, `GEMINI_MODEL_NAME`
- Cloudinary (bắt buộc nếu dùng upload ảnh): `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`, `CLOUDINARY_FOLDER`

Nếu `GEMINI_AI_KEY` rỗng/không set thì `/api/plan` sẽ fallback sang heuristic (không cần LLM).

## Map tài liệu
## Backend
- `docs/backend/01-auth.md`
- `docs/backend/02-profile.md`
- `docs/backend/03-plan.md`
- `docs/backend/04-dashboard.md`
- `docs/backend/05-meals.md`
- `docs/backend/06-calories.md`
- `docs/backend/07-weights.md`
- `docs/backend/08-images.md`
- `docs/backend/09-database.md`

## Frontend
- `docs/frontend/01-app.md`
- `docs/frontend/02-auth-ui.md`
- `docs/frontend/03-profile-ui.md`
- `docs/frontend/04-plan-ui.md`
- `docs/frontend/05-dashboard-ui.md`
- `docs/frontend/06-api-client.md`

