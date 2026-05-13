package com.vibe.finance.api;

import com.vibe.finance.store.FinanceStore;
import java.time.LocalDate;
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
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid transaction payload"));
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
            @RequestParam(name = "end_date", required = false) String endDate) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            return ResponseEntity.ok(financeStore.statistics(userId, period, startDate, endDate));
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
            
            // Đơn giản hóa danh sách danh mục để AI dễ đọc
            StringBuilder catsList = new StringBuilder();
            for (Map<String, Object> c : categories) {
                catsList.append(String.format("- ID: %s, Tên: %s, Loại: %s\n", c.get("id"), c.get("name"), c.get("type")));
            }
            
            String prompt = String.format(
                "Bạn là một trợ lý quản lý tài chính. Hãy phân tích câu sau để trích xuất thông tin giao dịch: \"%s\"\n\n" +
                "Danh sách danh mục (CHỈ ĐƯỢC CHỌN TỪ ĐÂY):\n%s\n\n" +
                "Yêu cầu:\n" +
                "1. Trả về DUY NHẤT một chuỗi JSON.\n" +
                "2. 'amount': Số tiền (phải là số nguyên, ví dụ: '210k' -> 210000, '1 triệu' -> 1000000). KHÔNG dùng dấu chấm hay phẩy.\n" +
                "3. 'description': Mô tả ngắn gọn giao dịch.\n" +
                "4. 'category_id': ID của danh mục phù hợp nhất từ danh sách trên.\n" +
                "5. 'transaction_date': Định dạng yyyy-MM-dd (Ngày hôm nay là %s).\n\n" +
                "Cấu trúc JSON: {\"amount\": number, \"description\": \"string\", \"category_id\": number, \"transaction_date\": \"string\"}",
                text, catsList.toString(), LocalDate.now().toString()
            );
            
            String aiResponse = aiService.generateResponse(prompt, true);
            System.out.println("AI Response raw: " + aiResponse);
            
            String jsonClean = aiResponse.replaceAll("(?s).*?(\\{.*\\}).*", "$1").trim();
            if (!jsonClean.startsWith("{")) {
                throw new RuntimeException("AI did not return valid JSON: " + aiResponse);
            }
            
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> txData = mapper.readValue(jsonClean, Map.class);
            System.out.println("Parsed TX Data: " + txData);
            
            // Xử lý số tiền (Amount)
            Object amountObj = txData.get("amount");
            if (amountObj == null) amountObj = txData.get("Amount");
            
            double amount = 0;
            if (amountObj != null) {
                String amtStrRaw = String.valueOf(amountObj).toLowerCase();
                String amtStrDigits = amtStrRaw.replaceAll("[^0-9]", "");
                if (!amtStrDigits.isEmpty()) {
                    amount = Double.parseDouble(amtStrDigits);
                    // Nếu chuỗi chứa 'k' và số đang nhỏ (như 210), nhân 1000
                    if (amtStrRaw.contains("k") && amount < 10000) {
                        amount *= 1000;
                    } else if ((amtStrRaw.contains("tr") || amtStrRaw.contains("triệu")) && amount < 1000) {
                        amount *= 1000000;
                    }
                }
            }
            
            // Xử lý danh mục (Category)
            Object categoryId = txData.get("category_id");
            if (categoryId == null || String.valueOf(categoryId).equals("null") || String.valueOf(categoryId).equals("1")) {
                // Nếu AI trả về ID 1 (Lương) nhưng câu nói là 'chi' hoặc 'mua' -> cần sửa lại
                String textLower = text.toLowerCase();
                boolean isExpense = textLower.contains("chi") || textLower.contains("hết") || textLower.contains("mua") || textLower.contains("mất");
                
                if (isExpense) {
                    if (textLower.contains("ăn") || textLower.contains("uống") || textLower.contains("sáng")) {
                        categoryId = categories.stream().filter(c -> String.valueOf(c.get("name")).toLowerCase().contains("an uong")).map(c -> c.get("id")).findFirst().orElse(null);
                    } else if (textLower.contains("xe") || textLower.contains("xăng") || textLower.contains("di chuyển")) {
                        categoryId = categories.stream().filter(c -> String.valueOf(c.get("name")).toLowerCase().contains("di chuyen")).map(c -> c.get("id")).findFirst().orElse(null);
                    }
                }
                
                // Nếu vẫn chưa tìm thấy hoặc bị nhầm sang Lương khi đang là chi tiêu
                if (categoryId == null || (isExpense && String.valueOf(categoryId).equals("1"))) {
                    categoryId = categories.stream()
                        .filter(c -> "Khac".equals(c.get("name")))
                        .map(c -> c.get("id"))
                        .findFirst()
                        .orElse(5L);
                }
            }
            
            Map<String, Object> createPayload = new HashMap<>();
            createPayload.put("amount", amount);
            createPayload.put("description", txData.getOrDefault("description", text));
            createPayload.put("category", categoryId);
            createPayload.put("transaction_date", txData.getOrDefault("transaction_date", LocalDate.now().toString()));
            
            System.out.println("Final Create Payload: " + createPayload);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(financeStore.createTransaction(userId, createPayload));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "AI Analysis Error: " + ex.getMessage()));
        }
    }
}
