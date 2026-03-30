# Setup Checklist (Dựng môi trường chạy được)

Mục tiêu: bạn làm theo đúng checklist này để app gọi API được và DB chạy ổn.

## 0) Chuẩn bị

1. Cài sẵn:
   - `Java 21` (backend build với Gradle, `sourceCompatibility=21`)
   - `Flutter SDK`
   - `PostgreSQL`
2. Kiểm tra nhanh:

```powershell
java -version
flutter --version
psql --version
```

## 1) Tạo database

1. Mở `psql`:

```powershell
psql -U postgres
```

2. Tạo DB (theo `DB_NAME` trong `backend/.env`):

```sql
CREATE DATABASE vibe_engineering;
```

## 2) Set backend `.env`

File: `backend/.env`

Bạn cần đảm bảo các biến sau tồn tại đúng với PostgreSQL của bạn:
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`

Phần tuỳ chọn:
- `GEMINI_AI_KEY` (nếu trống/không set thì `/api/plan` fallback heuristic)
- Cloudinary (`CLOUDINARY_*`) nếu bạn muốn upload ảnh món ăn

Lưu ý: tránh commit secrets lên git.

## 3) Cài dependencies

### Backend

Tại root:

```powershell
cd backend
.\gradlew.bat build
```

### Frontend

Tại root:

```powershell
cd frontend
flutter pub get
```

## 4) Chạy dev

Khuyến nghị dùng script có sẵn:

```powershell
cd ..  # quay về root nếu đang ở frontend/backend
.\run-dev.ps1 chrome
```

Nếu bạn muốn chạy tay (tách 2 terminal):

### Terminal 1: backend
```powershell
cd backend
.\gradlew.bat bootRun
```

### Terminal 2: frontend
```powershell
cd frontend
flutter run -d chrome
```

## 5) Xác nhận nhanh

Backend chạy ở:
- `http://localhost:8080`

Nếu bạn mở app Flutter và thấy lỗi “Connection refused”:
- backend chưa chạy
- hoặc frontend baseUrl bị sai (hiện tại đang hardcode `http://localhost:8080` trong `frontend/lib/main.dart`)

