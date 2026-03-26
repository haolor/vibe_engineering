# Dashboard UI (Charts + Meals Workbench)

## Điểm vào
- `frontend/lib/screens/dashboard_screen.dart` (`DashboardScreen`)

## Dữ liệu dashboard
- gọi `GET /api/dashboard` để lấy:
  - biểu đồ cân nặng 30 ngày gần đây
  - biểu đồ calo theo lộ trình (hoặc calo đã log nếu có)

## Các khối chức năng chính trong UI
### 1) Lưu cân nặng + lưu calo
- cân nặng:
  - `POST /api/weights`
  - sử dụng `_logDate` đang chọn (mặc định `DateTime.now()`)
- calo:
  - `POST /api/calories`
  - nếu chưa phân tích món trước thì lấy calories từ ô nhập tay
  - `mealAnalysisId` được truyền nếu `_lastMealAnalyze` có sẵn

### 2) Phân tích món ăn
- UI cho phép:
  - nhập `mealName`
  - chọn ảnh (gallery) -> upload -> lấy `imageUrl` từ Cloudinary
  - nhập `ingredients` dạng text từng dòng:
    - format: `ten:so_luong` (ví dụ `com:1`)
    - backend UI parse: tách theo `:` và cố parse `amount` thành double
- gọi `POST /api/meals/analyze`
- hiển thị `totalCalories` và list ingredient result

### 3) Gợi ý thay thế
- auto:
  - gọi `POST /api/meals/substitute/auto`
- manual:
  - nhập `substituteMealName` + ingredients
  - gọi `POST /api/meals/substitute/manual`
  - hiển thị `acceptable` + `note`

### 4) Đồng bộ món theo plan + chọn ngày
- `currentPlan.planJson.days` -> lọc theo ngày đang chọn
- dropdown cho phép chọn món trong plan của ngày đó
- khi chọn món:
  - set `originalCalories` (đưa vào ô để gọi auto/manual substitute)

## Dependency
- dùng `fl_chart` cho biểu đồ
- dùng `image_picker` để upload ảnh

