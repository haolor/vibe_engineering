# Vibe Engineering

Ứng dụng theo dõi dinh dưỡng gồm:
- **Backend**: Spring Boot API (Java, Postgres, Flyway)
- **Frontend**: Flutter app (web/mobile/desktop)

Mục tiêu chính của web/app:
- Đăng ký/đăng nhập người dùng
- Nhập hồ sơ BMI (chiều cao, cân nặng, tuổi, giới tính)
- Tạo lộ trình ăn uống theo mục tiêu tăng/giảm cân
- Theo dõi cân nặng và calories theo ngày (dashboard biểu đồ)
- Phân tích calories món ăn (nhập thành phần hoặc upload ảnh)
- Gợi ý món thay thế tự động/manual theo calories

---

## 1) Công nghệ sử dụng

- Backend: Java 21, Spring Boot, Spring Data JPA, Flyway, PostgreSQL
- Frontend: Flutter (Dart), `http`, `fl_chart`, `image_picker`
- Tích hợp ngoài: Cloudinary (upload ảnh), Gemini (optional, tạo meal plan)

---

## 2) Yêu cầu cài đặt

Máy dev cần có:
- Git
- **JDK 21**
- **Flutter SDK** (kèm Dart)
- **PostgreSQL** (khuyến nghị 14+)

Kiểm tra nhanh:

```powershell
java -version
flutter --version
psql --version
```

---

## 3) Cài dependencies / libraries

### Backend libraries (Gradle)

Từ thư mục `backend`:

```powershell
.\gradlew.bat build
```

Lệnh trên sẽ tải toàn bộ dependency backend (Spring Boot, JPA, Flyway, PostgreSQL driver, ...).

### Frontend libraries (Flutter pub)

Từ thư mục `frontend`:

```powershell
flutter pub get
```

Lệnh trên sẽ cài các package trong `pubspec.yaml` như `http`, `fl_chart`, `image_picker`.

---

## 4) Cấu hình môi trường

Backend đọc config từ file `backend/.env`.

Ví dụ:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=vibe_engineering
DB_USER=postgres
DB_PASSWORD=your_password

GEMINI_AI_KEY=
GEMINI_MODEL_NAME=gemini-2.5-flash

# OpenRouter (tuỳ chọn). Nếu set thì backend sẽ ưu tiên dùng trước Gemini.
OPENROUTER_API_KEY=
OPENROUTER_MODEL_NAME=openai/gpt-oss-120b
OPENROUTER_BASE_URL=https://openrouter.ai/api/v1

CLOUDINARY_CLOUD_NAME=...
CLOUDINARY_API_KEY=...
CLOUDINARY_API_SECRET=...
CLOUDINARY_FOLDER=vibe_engineering
```

Lưu ý:
- Nếu `OPENROUTER_API_KEY` trống/không set, backend sẽ dùng Gemini (nếu `GEMINI_AI_KEY` có).
- Nếu cả OpenRouter và Gemini đều không cấu hình, backend sẽ tự fallback qua heuristic khi tạo plan.
- Không nên commit secrets thật vào git.

---

## 5) Tạo database

Tạo DB trước khi chạy backend (tên theo `.env`):

```sql
CREATE DATABASE vibe_engineering;
```

Khi backend chạy, Flyway sẽ tự tạo schema từ migration:
- `backend/src/main/resources/db/migration/V1__init.sql`

---

## 6) Chạy ứng dụng

## Cách nhanh (khuyên dùng)

Tại root project:

```powershell
.\run-dev.ps1 chrome
```

Script sẽ:
1. Start backend (`gradlew.bat bootRun`)
2. Start frontend (`flutter run -d chrome`)

## Chạy tay từng phần

### Start backend

```powershell
cd backend
.\gradlew.bat bootRun
```

Backend mặc định chạy ở:
- `http://localhost:8080`

### Start frontend

Mở terminal mới:

```powershell
cd frontend
flutter run -d chrome
```

---

## 7) Luồng sử dụng cơ bản

1. Mở app, đăng ký hoặc đăng nhập
2. Vào tab **Profile** để nhập BMI/hồ sơ
3. Vào tab **Plan** để tạo lộ trình ăn uống
4. Vào tab **Dashboard** để:
   - theo dõi biểu đồ cân nặng/calo
   - phân tích món ăn
   - gợi ý món thay thế
   - log cân nặng và calories theo ngày

---

## 8) Tài liệu chi tiết theo chức năng

Xem thư mục `docs/`:
- `docs/00-overview.md`
- `docs/backend/*.md`
- `docs/frontend/*.md`

---

## 9) Một số lỗi hay gặp

- `API error ... Connection refused`
  - Backend chưa chạy, hoặc sai `baseUrl` ở frontend (`main.dart`)
- `Chưa có profile...`
  - Chưa nhập hồ sơ ở tab Profile
- Lỗi DB connect
  - Kiểm tra PostgreSQL có chạy không
  - Kiểm tra `DB_HOST/PORT/NAME/USER/PASSWORD` trong `backend/.env`
- Upload ảnh lỗi
  - Kiểm tra cấu hình Cloudinary

