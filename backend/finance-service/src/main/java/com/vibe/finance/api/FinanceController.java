package com.vibe.finance.api;

import com.vibe.finance.store.FinanceStore;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.vibe.finance.service.AIService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping
public class FinanceController {
    private final FinanceStore financeStore;
    private final AIService aiService;

    public FinanceController(FinanceStore financeStore, AIService aiService) {
        this.financeStore = financeStore;
        this.aiService = aiService;
    }

    @GetMapping("/categories/")
    public List<Map<String, Object>> categories() {
        return financeStore.categories();
    }

    @PostMapping("/categories/")
    public ResponseEntity<?> createCategory(@RequestBody Map<String, Object> payload) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(financeStore.createCategory(payload));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid category payload"));
        }
    }

    @GetMapping("/transactions/")
    public ResponseEntity<?> list(
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestParam(name = "page", defaultValue = "1") int page) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            return ResponseEntity.ok(financeStore.listTransactions(userId, page));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error: " + ex.getMessage()));
        }
    }

    @PostMapping("/transactions/")
    public ResponseEntity<?> create(
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestBody Map<String, Object> payload) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(financeStore.createTransaction(userId, payload));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid transaction payload: " + ex.getMessage()));
        }
    }

    @PutMapping("/transactions/{id}/")
    public ResponseEntity<?> update(
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @PathVariable(name = "id") long id,
            @RequestBody Map<String, Object> payload) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            return ResponseEntity.ok(financeStore.updateTransaction(userId, id, payload));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid transaction payload"));
        }
    }

    @DeleteMapping("/transactions/{id}/")
    public ResponseEntity<?> delete(
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @PathVariable(name = "id") long id) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        financeStore.deleteTransaction(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/transactions/statistics/")
    public ResponseEntity<?> statistics(
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestParam(name = "period", required = false) String period,
            @RequestParam(name = "start_date", required = false) String startDate,
            @RequestParam(name = "end_date", required = false) String endDate,
            @RequestParam(name = "category_ids", required = false) List<Long> categoryIds) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            return ResponseEntity.ok(financeStore.statistics(userId, period, startDate, endDate, categoryIds));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error: " + ex.getMessage()));
        }
    }

    @GetMapping("/transactions/expenses/")
    public ResponseEntity<?> expenses(@RequestHeader(name = "X-User-Id", required = false) String userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            return ResponseEntity.ok(financeStore.recentExpenses(userId));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error: " + ex.getMessage()));
        }
    }

    @PostMapping("/transactions/nlp_query/")
    public ResponseEntity<?> nlpQuery(
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestBody Map<String, String> payload) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            String text = payload.get("text");
            System.out.println("[FinanceNLP] Request from userId: " + userId + " - Text: " + text);
            
            // Save user message to history
            financeStore.saveChatMessage(userId, "user", text);
            
            // Lấy dữ liệu giao dịch gần đây để ai có ngữ cảnh
            List<Map<String, Object>> stats = financeStore.recentExpenses(userId);
            String prompt = String.format(
                "Dưới đây là dữ liệu chi tiêu gần đây của người dùng: %s\n\nNgười dùng hỏi: %s\n\nHãy trả lời ngắn gọn, thân thiện.",
                stats.toString(), text
            );
            String response = aiService.generateResponse(prompt, true);
            
            // Save bot response to history
            financeStore.saveChatMessage(userId, "bot", response);
            System.out.println("[FinanceNLP] Saved interaction for userId: " + userId);
            
            return ResponseEntity.ok(Map.of("result", response));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error: " + ex.getMessage()));
        }
    }

    @PostMapping("/transactions/nlp_input/")
    public ResponseEntity<?> nlpInput(
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestBody Map<String, String> payload) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            String text = payload.get("text");
            List<Map<String, Object>> categories = financeStore.categories();
            
            StringBuilder catsList = new StringBuilder();
            for (Map<String, Object> c : categories) {
                catsList.append(String.format("- ID: %s, Tên: %s, Loại: %s\n", c.get("id"), c.get("name"), c.get("type")));
            }
            
            String prompt = String.format(
                "Bạn là một máy trích xuất dữ liệu tài chính. NHIỆM VỤ: Phân tích câu mô tả và trả về danh sách các giao dịch dưới dạng JSON ARRAY.\n" +
                "Nếu câu chứa nhiều sự kiện (ví dụ: nhận tiền rồi tiêu tiền), hãy tách chúng thành các giao dịch riêng biệt.\n" +
                "KHÔNG giải thích, KHÔNG nói gì thêm.\n\n" +
                "ĐẦU VÀO:\n" +
                "- Câu: \"%s\"\n" +
                "- Ngày hôm nay: %s\n" +
                "- Danh mục (ID|Tên|Loại):\n%s\n\n" +
                "QUY TẮC:\n" +
                "1. 'amount': Số nguyên dương.\n" +
                "2. 'category_id': ID danh mục phù hợp. Lưu ý: Thu nhập (income) vs Chi phí (expense).\n" +
                "3. 'description': Mô tả ngắn gọn.\n" +
                "4. 'transaction_date': yyyy-MM-dd.\n\n" +
                "VÍ DỤ:\n" +
                "Câu: \"Mẹ cho 100k, ăn sáng 45k\"\n" +
                "JSON: [\n" +
                "  {\"amount\": 100000, \"description\": \"Mẹ cho\", \"category_id\": 4, \"transaction_date\": \"%s\"},\n" +
                "  {\"amount\": 45000, \"description\": \"Ăn sáng\", \"category_id\": 2, \"transaction_date\": \"%s\"}\n" +
                "]\n\n" +
                "TRẢ VỀ JSON ARRAY:",
                text, LocalDate.now().toString(), catsList.toString(), LocalDate.now().toString(), LocalDate.now().toString()
            );
            
            String aiResponse = aiService.generateResponse(prompt, false);
            String jsonClean = aiResponse.replaceAll("(?s).*?(\\[.*?\\]|\\{.*?\\}).*", "$1").trim();
            
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            List<Map<String, Object>> txDataList = new ArrayList<>();
            Object parsed = mapper.readValue(jsonClean, Object.class);
            if (parsed instanceof List) {
                txDataList = (List<Map<String, Object>>) parsed;
            } else if (parsed instanceof Map) {
                txDataList.add((Map<String, Object>) parsed);
            }

            List<Map<String, Object>> results = new ArrayList<>();
            for (Map<String, Object> txData : txDataList) {
                double amount = parseAmount(txData);
                Object categoryId = resolveCategory(txData, text, categories);
                
                Map<String, Object> createPayload = new HashMap<>();
                createPayload.put("amount", amount);
                createPayload.put("description", txData.getOrDefault("description", text));
                createPayload.put("category", categoryId);
                createPayload.put("transaction_date", txData.getOrDefault("transaction_date", LocalDate.now().toString()));
                
                results.add(financeStore.createTransaction(userId, createPayload));
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(results);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "AI Analysis Error: " + ex.getMessage()));
        }
    }

    @PostMapping("/transactions/ocr_receipt/")
    public ResponseEntity<?> ocrReceipt(
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestParam("image") MultipartFile image) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            byte[] bytes = image.getBytes();
            String base64 = "data:" + image.getContentType() + ";base64," + java.util.Base64.getEncoder().encodeToString(bytes);
            
            List<Map<String, Object>> categories = financeStore.categories();
            StringBuilder catsList = new StringBuilder();
            for (Map<String, Object> c : categories) {
                catsList.append(String.format("- ID: %s, Tên: %s, Loại: %s\n", c.get("id"), c.get("name"), c.get("type")));
            }

            String prompt = "Phân tích ảnh hóa đơn này và trích xuất thông tin giao dịch.\n" +
                "Trả về DUY NHẤT JSON theo cấu trúc sau:\n" +
                "{\n" +
                "  \"amount\": number,\n" +
                "  \"description\": \"string\",\n" +
                "  \"category_id\": number,\n" +
                "  \"transaction_date\": \"yyyy-MM-dd\",\n" +
                "  \"merchant_name\": \"string\"\n" +
                "}\n\n" +
                "Danh sách danh mục:\n" + catsList.toString();

            String aiResponse = aiService.generateVisionResponse(prompt, base64);
            System.out.println("[OCR] AI Raw Response: " + aiResponse);
            
            String jsonClean = aiResponse.replaceAll("(?s).*?(\\{.*\\}).*", "$1").trim();
            if (!jsonClean.startsWith("{")) {
                throw new RuntimeException("AI did not return valid JSON for OCR: " + aiResponse);
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> data = mapper.readValue(jsonClean, Map.class);
            
            double amount = parseAmount(data);
            Object categoryId = resolveCategory(data, "", categories);
            
            Map<String, Object> createPayload = new HashMap<>();
            createPayload.put("amount", amount);
            createPayload.put("description", data.getOrDefault("description", data.getOrDefault("merchant_name", "Giao dịch từ hóa đơn")));
            createPayload.put("category", categoryId);
            createPayload.put("transaction_date", data.getOrDefault("transaction_date", LocalDate.now().toString()));
            
            Map<String, Object> result = financeStore.createTransaction(userId, createPayload);
            
            Map<String, Object> response = new HashMap<>();
            response.put("transaction", result);
            response.put("extracted_info", data);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "OCR Analysis Error: " + ex.getMessage()));
        }
    }

    private double parseAmount(Map<String, Object> txData) {
        Object amountObj = txData.get("amount");
        if (amountObj == null) amountObj = txData.get("Amount");
        
        if (amountObj instanceof Number) {
            return ((Number) amountObj).doubleValue();
        } else if (amountObj != null) {
            String amtStrRaw = String.valueOf(amountObj).toLowerCase().trim();
            String cleanAmt = amtStrRaw.replaceAll("[^0-9.]", "");
            if (!cleanAmt.isEmpty()) {
                try {
                    double amount = Double.parseDouble(cleanAmt);
                    if ((amtStrRaw.contains("k") || amtStrRaw.contains("nghìn") || amtStrRaw.contains("ngàn")) && amount < 10000) {
                        amount *= 1000;
                    } else if ((amtStrRaw.contains("tr") || amtStrRaw.contains("triệu")) && amount < 1000) {
                        amount *= 1000000;
                    }
                    return amount;
                } catch (Exception ignored) {}
            }
        }
        return 0;
    }

    private Object resolveCategory(Map<String, Object> txData, String originalText, List<Map<String, Object>> categories) {
        Object categoryId = txData.get("category_id");
        String description = String.valueOf(txData.getOrDefault("description", "")).toLowerCase();
        
        // Nếu AI trả về ID 1 (Lương) mặc định nhưng mô tả là chi tiêu
        if (categoryId == null || String.valueOf(categoryId).equals("1")) {
            if (description.contains("ăn") || description.contains("uống") || description.contains("sáng") || description.contains("trưa") || description.contains("tối")) {
                return categories.stream().filter(c -> String.valueOf(c.get("name")).toLowerCase().contains("an uong")).map(c -> c.get("id")).findFirst().orElse(2L);
            }
            if (description.contains("xe") || description.contains("xăng") || description.contains("grab") || description.contains("đi lại")) {
                return categories.stream().filter(c -> String.valueOf(c.get("name")).toLowerCase().contains("di chuyen")).map(c -> c.get("id")).findFirst().orElse(3L);
            }
            if (description.contains("mẹ cho") || description.contains("ba cho") || description.contains("được cho")) {
                return 4L; // Thưởng/Khác (Income)
            }
        }
        return categoryId != null ? categoryId : 5L; // Mặc định là Khác
    }
}
