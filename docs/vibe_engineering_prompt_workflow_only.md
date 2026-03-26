# Vibe Engineering - Prompt Workflow Only (Prompt -> Code -> Self-correct)

Tài liệu này chỉ chứa **các prompt workflow** (SYSTEM/USER) để đưa cho AI đóng vai trò là coding agent.
Mục tiêu: minh họa việc áp dụng vibe engineering bằng chuỗi
**Prompt -> AI sinh code/draft -> AI tự kiểm tra -> AI sinh bản chỉnh sửa cuối cùng**.

---

## Prompt #1 - Tách requirement thành artifact list (API/DTO/DB/test)

```md
SYSTEM:
Bạn là senior software architect cho Spring Boot + Flutter.
Bạn làm việc như một coding agent: tạo danh sách artifact phải có và mô tả output theo đúng repo.

USER:
Hãy dựa vào các tài liệu trong repo:
- `docs/00-overview.md`
- `docs/backend/*.md`
- `docs/frontend/*.md`
và đối chiếu với code backend hiện có (controller/service/entity/repository).

Yêu cầu hệ thống (App Nutrition):
1) BMI/Profile: nhập chiều cao (cm/ft), cân nặng (kg/lb), tuổi, giới tính (Nam/Nu).
   Tính BMI và lưu Postgres.
2) Plan: nhập nhu cầu (TANG/GIAM), targetWeightKg, timeframe (ngay/tuan/thang/nam).
   Nếu Gemini sẵn sàng thì tạo meal plan bằng prompt JSON; nếu không thì fallback heuristic.
3) Meal analyze: người dùng nhập ingredients hoặc gửi ảnh.
   Trả về totalCalories và chi tiết calories theo từng ingredient.
4) Substitute: gợi ý món thay thế tương đương calories (auto/manual) và thông báo acceptable.
5) Dashboard: biểu đồ lịch sử cân nặng + calo theo thời gian.

Nhiệm vụ:
- Sinh "Artifact Plan" gồm: endpoints, DTOs, entities/repositories (nếu cần), migrations Flyway (nếu cần), test cases.
- Mỗi artifact phải có: path/class/method (nếu đã tồn tại) hoặc đề xuất path/class/method mới.

OUTPUT REQUIREMENTS:
1) Endpoint table: columns = HTTP method, path, request fields, response fields
2) DTO table: columns = DTO name, fields, validation notes
3) Migration plan: columns = table, columns, indexes, FK notes
4) Test plan: columns = test name, inputs, expected behavior
5) Section "Gap to fit đề tài nâng cấp": liệt kê những thiếu hụt so với yêu cầu nutrition DB + vision extraction (nếu hiện tại repo chưa có).

Constraint:
- Không nói chung chung. Luôn trỏ tới file/class/path cụ thể trong repo nếu có.
```

---

## Prompt #2 - Thiết kế nutrition DB schema + Flyway migration (draft -> self-check -> final)

```md
SYSTEM:
Bạn là database architect + prompt-to-DDL generator.
Bạn phải tạo migration SQL Flyway theo dạng có thể chạy.
Sau khi tạo draft, bạn tự kiểm tra ràng buộc, indexes, và output cuối cùng là version đã chỉnh.

USER:
Trong app Nutrition, calories cần được tính từ nutrition/food database (không dùng hardcoded calories catalog).
Hãy thiết kế tối thiểu nutrition DB trong Postgres và tạo Flyway migration.

Bối cảnh repo:
- Migration hiện tại nằm ở: `backend/src/main/resources/db/migration/V1__init.sql`
- Meal analysis hiện lưu `meal_analysis`, `meal_ingredient`, có field `cloudinary_public_id` trong entity `MealAnalysis`.
- Các tài liệu: `docs/backend/05-meals.md`, `docs/backend/09-database.md`

Yêu cầu nutrition DB tối thiểu:
- Có canonical name + alias mapping để match tên ingredient từ Vision/user.
- Có calories theo 100g (hoặc theo baseline serving) để tính:
  calories = calories_per_100g * grams/100
- Có unit conversion:
  quantity + unit -> grams (g/ml/tbsp/portion hoặc tương đương)
- Có indexes cho lookup nhanh theo alias_text.

OUTPUT:
1) Draft version: cung cấp SQL `CREATE TABLE` + `CREATE INDEX` (tách đoạn)
2) Self-check: ít nhất 8 điểm kiểm tra (FK/nullable/index/unit precision/unique constraints)
3) Final version: sửa các điểm fail ở self-check và xuất SQL cuối cùng
4) Map sang code: liệt kê các bảng nào phục vụ `NutritionLookupService` lookup calories theo flow analyze/substitute.

Constraint:
- Viết SQL dạng ANSI/Postgres.
- Đặt tên constraint/index rõ ràng.
```

---

## Prompt #3 - Sinh NutritionLookupService (code v1 -> tự kiểm tra -> code v2)

```md
SYSTEM:
Bạn là backend engineer Java 21 + Spring Boot.
Bạn làm việc theo vòng:
v1 code draft -> self-check -> v2 code chỉnh để khớp repo.

USER:
Mục tiêu: thay thế logic hardcoded calories trong `MealService` bằng DB nutrition lookup.

Bối cảnh repo:
- File: `backend/src/main/java/com/example/backend/service/MealService.java`
- Entity/DB hiện lưu meal analysis: `MealAnalysis`, `MealIngredient`
- DTO đầu vào/ra meal analyze: `MealAnalyzeRequest`, `MealIngredientInputDto`, `MealIngredientResultDto` (nêu chính xác theo repo)
- Nutrition DB sẽ được thêm qua migration (bạn dùng schema từ Prompt #2).

Nhiệm vụ:
- Thiết kế và tạo class `NutritionLookupService` (nếu chưa có) gồm:
  - method `List<MealIngredientResultDto> calculateCalories(List<MealIngredientInputDto> inputs)`
  - internal lookup theo `food_alias.alias_text` -> `food_item` -> `food_nutrition`
  - convert quantity/unit -> grams -> calories
- Tích hợp vào `MealService.analyze()`:
  - Nếu ingredients từ user đầy đủ: dùng NutritionLookupService
  - Nếu ingredients thiếu/amount không hợp lệ và có imageUrl: (trong prompt này chỉ chuẩn bị interface/hook; vision extraction sẽ ở Prompt #4)

OUTPUT REQUIREMENTS (bắt buộc đủ):
1) Danh sách file sẽ tạo/sửa (path đầy đủ)
2) Code v1 (Java) theo từng file, có code block và heading rõ ràng
3) Self-check (ít nhất 10 điểm):
   - JSON/DTO compatibility (field names)
   - unit conversion edge cases
   - alias normalization
   - exception types/messages (liên quan `ApiExceptionHandler`)
   - performance notes (indexes, N+1 queries)
4) Code v2 (chỉnh lại theo self-check)
5) Unit test plan + test code (JUnit 5) cho 3 case:
   - alias exact match
   - unit conversion portion->grams
   - không match alias -> exception

Constraint:
- Không dùng hardcoded calories catalog.
- Code phải biên dịch theo style Spring Data JPA trong repo.
```

---

## Prompt #4 - Vision extraction pipeline (Gemini Vision hoặc model khác) -> ingredients JSON

```md
SYSTEM:
Bạn là chuyên gia thiết kế prompt ép JSON và implement parsing/validation trong backend.
Bạn phải tự kiểm tra JSON schema và fallback policy.

USER:
Mục tiêu: từ ảnh món ăn (imageUrl) suy ra ingredients JSON chuẩn để sau đó tính calories qua Nutrition DB.

Bối cảnh repo:
- Cloudinary upload đang có: `backend/src/main/java/com/example/backend/service/CloudinaryService.java`
- Analyze endpoint hiện tại: `POST /api/meals/analyze`
- Hiện repo đang có `GeminiMealPlanService` (text JSON plan). Không giả định vision đã có.
- Tài liệu: `docs/backend/05-meals.md`, `docs/backend/08-images.md`

Nhiệm vụ:
1) Định nghĩa JSON schema ingredients từ ảnh:
   - mealName
   - ingredients[] gồm {name, quantity, unit, confidence}
2) Thiết kế prompt cho Vision:
   - output chỉ duy nhất JSON hợp lệ, không markdown
   - unit trong whitelist: g/ml/tbsp/portion
3) Thiết kế class/service backend:
   - `GeminiVisionMealExtractionService` (hoặc tên tương đương)
   - method `ExtractedIngredients extract(String imageUrl)`
4) Thiết kế validation:
   - validate mandatory fields
   - normalize unit
   - fallback policy nếu Vision thiếu quantity/unit

OUTPUT:
1) Vision prompt (copy/paste)
2) Code v1 skeleton cho service + parsing/validation
3) Self-check: 10+ điểm kiểm tra parse JSON, validate whitelist unit, policy fallback
4) Code v2 final
5) Integration test plan (mô tả test case và expected output; không cần mock external call nếu chưa có hạ tầng)

Constraint:
- Không dùng heuristics hardcoded calories trong prompt này.
- JSON schema phải khớp phần nutrition lookup trong Prompt #3.
```

---

## Prompt #5 - Refactor flow analyze (MealService) để dùng vision + nutrition DB (v1 -> self-correct -> v2)

```md
SYSTEM:
Bạn là tech lead refactor.
Bạn tự kiểm tra tính đúng luồng và tương thích request/response hiện có.

USER:
Refactor `MealService.analyze()` để đạt luồng:
1) Nếu ingredients user đầy đủ -> NutritionLookupService tính calories
2) Nếu ingredients thiếu và imageUrl có -> Vision extraction -> NutritionLookupService tính calories
3) Lưu DB:
   - `meal_analysis.total_calories`
   - `meal_ingredient` theo ingredient results
   - set `cloudinary_public_id` nếu request có publicId (bạn thêm field vào request/DTO nếu cần)

Bối cảnh repo hiện có:
- `backend/src/main/java/com/example/backend/service/MealService.java` (hiện đang dùng CALORIES_CATALOG)
- `backend/src/main/resources/db/migration/V1__init.sql` và entity `MealAnalysis` có `cloudinary_public_id`
- Frontend hiện gửi `secureUrl` trong `MealAnalyzeRequest.imageUrl` (nếu cần publicId, hãy đề xuất thay đổi API client + DTO tương ứng)

OUTPUT:
1) Danh sách file cần sửa (backend + frontend + docs nếu cần)
2) Code v1 cho refactor (đủ để biên dịch về mặt cấu trúc)
3) Self-check theo checklist:
   - không phá response format hiện tại
   - không tạo vòng lặp gọi service
   - transaction đúng với save DB
   - mapping cloudinary publicId -> MealAnalysis.cloudinaryPublicId
   - đảm bảo fallback policy khi Vision không trả đủ ingredients
4) Code v2 final
5) Gợi ý cập nhật docs:
   - update `docs/backend/05-meals.md` và `docs/frontend/05-dashboard-ui.md` (chỉ nêu thay đổi, không viết dài)

Constraint:
- Không để lại hardcoded calories catalog làm nguồn tính calories chính.
```

---

## Prompt #6 - Đánh giá hiệu quả sử dụng AI trong phát triển (time/LOC/edit rate)

```md
SYSTEM:
Bạn là project analyst. Bạn tạo template đánh giá hiệu quả AI (có ô để điền số liệu thực tế).

USER:
Hãy tạo template báo cáo cho dự án App Nutrition để đánh giá hiệu quả sử dụng AI trong dev.
Các chỉ số cần:
1) Thời gian: baseline vs AI-assisted (T_baseline, T_ai_assisted, ΔT)
2) LOC: LOC_generated, LOC_edited, LOC_new_total
3) Edit rate: LOC_edited / LOC_generated
4) Chất lượng: test pass rate, số lần fail do AI sai assumption, mức độ sửa logic (parsing/validation/db constraints)

OUTPUT:
- 1 bảng template (có cột Task, T_baseline, T_ai_assisted, LOC_generated, LOC_edited, Edit rate, Notes)
- checklist để thu thập số liệu (chỉ liệt kê cách lấy từ git diff/log; không cần chạy command)
- phần hướng dẫn viết 1 đoạn kết luận mẫu (3-5 câu) dựa trên số liệu điền vào.

Constraint:
- Không bịa số liệu. Tất cả giá trị là TBD trừ khi người dùng cung cấp.
```

---

## Gợi ý chèn vào báo cáo
- Dùng file này để hiển thị “prompt workflow”.
- Đưa thêm 1-2 prompt tiêu biểu trong file báo cáo và ghi rõ output mong đợi theo từng prompt.

