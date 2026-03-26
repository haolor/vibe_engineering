# Auth (Register/Login)

## Endpoint
- `POST /api/auth/register`
- `POST /api/auth/login`

## Request/Response
### Register
`RegisterRequest` (JSON)
- `email`: string
- `password`: string
- `fullName`: string | null (tuỳ chọn)

Trả về: `AuthResponse`
- `userId`: int
- `email`: string
- `fullName`: string | null
- `message`: string

### Login
`LoginRequest` (JSON)
- `email`: string
- `password`: string

Trả về: `AuthResponse` giống trên.

## Luồng xử lý (code)
- `AuthController` chỉ nhận request và gọi `AuthService`.
- `AuthService`
  - normalize email: trim + lowercase
  - register:
    - kiểm tra email đã tồn tại bằng `UserAccountRepository.existsByEmailIgnoreCase`
    - hash password bằng `BCryptPasswordEncoder`
    - lưu `UserAccount`
  - login:
    - tìm user theo email
    - so sánh password bằng `passwordEncoder.matches`

## Lưu ý lỗi
Xem `ApiExceptionHandler`:
- `IllegalArgumentException` -> `400 BAD_REQUEST`
- exception khác -> `500 INTERNAL_ERROR`

