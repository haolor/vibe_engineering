# Auth UI (Register/Login)

## Điểm vào
- `frontend/lib/screens/auth_screen.dart` (`AuthScreen`)

## Hành vi
- Có toggle giữa `Đăng ký` và `Đăng nhập`
- Validation cơ bản:
  - email/password không được rỗng
  - khi register: password có tối thiểu 6 ký tự

## Gọi API
Thông qua `ApiClient`:
- register -> `POST /api/auth/register`
- login -> `POST /api/auth/login`

Khi thành công -> gọi callback `onAuthenticated(auth)` để app bootstrap tiếp.

