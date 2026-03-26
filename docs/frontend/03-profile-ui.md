# Profile UI (Nhập BMI)

## Điểm vào
- `frontend/lib/screens/profile_screen.dart` (`ProfileScreen`)

## Các field chính
- `heightCm` hoặc `heightFt`
- `weightKg` hoặc `weightLbs`
- `age`
- `gender`: `"Nam"`/`"Nữ"`

## Hành vi submit
- Validate:
  - `age` phải parse được
  - cần có 1 trong 2 đơn vị cho chiều cao và 1 trong 2 đơn vị cho cân nặng
- Gọi:
  - `POST /api/profile`

## Response được dùng để làm gì
- cập nhật `_bmi = resp.bmi` để hiển thị BMI
- gọi `onProfileSaved(resp.profileId)` để app lưu `profileId` cho các tab khác

