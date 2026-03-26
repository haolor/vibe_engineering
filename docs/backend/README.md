# Backend Docs (Spring Boot)

Mục tiêu của tài liệu này là “vibe lại” backend theo đúng luồng endpoint/mục tính năng.

Các endpoint đều nằm trong các `*Controller` và logic chính nằm trong `*Service`.

Xử lý lỗi:
- `IllegalArgumentException` -> `400 BAD_REQUEST` với body `{ error, message }`
- các lỗi khác -> `500 INTERNAL_ERROR` với body `{ error, message }`

