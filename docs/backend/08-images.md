# Image Upload (Cloudinary)

## Endpoint
- `POST /api/images/upload` (multipart/form-data)

## Request
- `file`: upload file ảnh (form field name phải đúng `"file"`)

## Response
`ImageUploadResponse`
- `secureUrl`: string
- `publicId`: string

## Luồng xử lý
- `ImageController` nhận `MultipartFile file` và gọi `CloudinaryService.uploadImage`
- `CloudinaryService.uploadImage`:
  - validate file không rỗng
  - đọc cấu hình Cloudinary từ env (`cloudinary.*`)
  - encode file sang `dataUri` base64
  - tự tạo chữ ký SHA-1 theo Cloudinary (folder, timestamp, apiSecret)
  - POST tới Cloudinary upload API
  - parse JSON response lấy `secure_url` + `public_id`

## Ghi chú
- Nếu thiếu cấu hình Cloudinary hoặc Cloudinary trả lỗi -> backend ném exception -> lỗi 4xx/5xx theo `ApiExceptionHandler`.

