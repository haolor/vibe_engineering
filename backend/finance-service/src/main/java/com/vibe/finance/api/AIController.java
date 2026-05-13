package com.vibe.finance.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Qualifier;

@RestController
@RequestMapping("/ai")
public class AIController {
    private final WebClient notificationWebClient;
    private final com.vibe.finance.store.FinanceStore financeStore;
    private final com.vibe.finance.service.AIService aiService;

    public AIController(
            @Qualifier("loadBalancedWebClientBuilder") org.springframework.web.reactive.function.client.WebClient.Builder loadBalancedWebClientBuilder,
            com.vibe.finance.store.FinanceStore financeStore,
            com.vibe.finance.service.AIService aiService) {
        this.notificationWebClient = loadBalancedWebClientBuilder.baseUrl("http://notification-service/notifications").build();
        this.financeStore = financeStore;
        this.aiService = aiService;
    }

    @GetMapping("/anomalies/")
    public ResponseEntity<?> anomalies(@RequestHeader(name = "X-User-Id", required = false) String userId) {
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        try {
            Map<String, Object> response = notificationWebClient.get()
                .uri("/")
                .header("X-User-Id", userId)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            List<Map<String, Object>> anomalies = new ArrayList<>();

            if (results != null) {
                for (Map<String, Object> n : results) {
                    if ("anomaly".equals(n.get("type")) || "large_transaction".equals(n.get("type"))) {
                        Map<String, Object> metadata = (Map<String, Object>) n.get("metadata");
                        Map<String, Object> anomaly = new java.util.HashMap<>();
                        anomaly.put("id", n.get("id"));
                        anomaly.put("category", metadata != null ? metadata.getOrDefault("category", "Bất thường") : "Bất thường");
                        anomaly.put("description", n.get("message"));
                        anomaly.put("date", n.get("created_at"));
                        anomaly.put("amount", metadata != null ? metadata.getOrDefault("amount", 0) : 0);
                        anomaly.put("deviation", metadata != null ? metadata.getOrDefault("deviation", 0) : 0);
                        anomalies.add(anomaly);
                    }
                }
            }

            return ResponseEntity.ok(Map.of("anomalies", anomalies));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("anomalies", new ArrayList<>()));
        }
    }

    @GetMapping("/trends/")
    public ResponseEntity<?> trends(@RequestHeader(name = "X-User-Id") String userId) {
        List<Map<String, Object>> transactions = financeStore.getAllTransactions(userId);
        
        if (transactions == null || transactions.isEmpty()) {
            return ResponseEntity.ok(Map.of("trend", "stable", "trend_percentage", 0, "weekly_data", new ArrayList<>()));
        }

        System.out.println("AI Trends: Analyzing " + transactions.size() + " transactions");

        String prompt = "Dưới đây là danh sách giao dịch tài chính của tôi: " + transactions.toString() + 
                        "\nPhân tích xu hướng chi tiêu tổng quát. Trả về JSON: {\"trend\": \"increasing|decreasing|stable\", \"trend_percentage\": 15.5}";
        
        String aiResponse = aiService.generateResponse(prompt);
        System.out.println("AI Trends Response: " + aiResponse);

        try {
            String jsonPart = extractJson(aiResponse);
            Map<String, Object> result = new com.fasterxml.jackson.databind.ObjectMapper().readValue(jsonPart, Map.class);
            
            // Lấy dữ liệu thống kê để vẽ biểu đồ
            Map<String, Object> stats = financeStore.statistics(userId, "all", null, null);
            result.put("weekly_data", stats.get("by_date")); 
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error parsing AI trends: " + e.getMessage());
            return ResponseEntity.ok(Map.of("trend", "stable", "trend_percentage", 0, "weekly_data", new ArrayList<>()));
        }
    }

    @GetMapping("/predictions/")
    public ResponseEntity<?> predictions(@RequestHeader(name = "X-User-Id") String userId) {
        List<Map<String, Object>> transactions = financeStore.getAllTransactions(userId);

        if (transactions == null || transactions.isEmpty()) {
            return ResponseEntity.ok(Map.of("predicted_amount", 0, "confidence", "N/A", "based_on_months", 0));
        }

        System.out.println("AI Predictions: Analyzing " + transactions.size() + " transactions for user " + userId);

        String prompt = "Dựa trên các giao dịch tài chính này: " + transactions.toString() + 
                        "\nHãy dự báo tổng chi tiêu cho tháng tới. Trả về kết quả DUY NHẤT dưới dạng JSON: {\"predicted_amount\": 5000000, \"confidence\": \"high|medium|low\", \"based_on_months\": 3}";

        String aiResponse = aiService.generateResponse(prompt);
        System.out.println("AI Predictions Response: " + aiResponse);

        try {
            String jsonPart = extractJson(aiResponse);
            return ResponseEntity.ok(new com.fasterxml.jackson.databind.ObjectMapper().readValue(jsonPart, Map.class));
        } catch (Exception e) {
            System.err.println("Error parsing AI prediction response: " + e.getMessage());
            return ResponseEntity.ok(Map.of("predicted_amount", 0, "confidence", "Lỗi phân tích", "based_on_months", 0));
        }
    }

    @GetMapping("/savings-suggestions/")
    public ResponseEntity<?> savings(@RequestHeader(name = "X-User-Id") String userId) {
        List<Map<String, Object>> transactions = financeStore.getAllTransactions(userId);

        if (transactions == null || transactions.isEmpty()) {
            return ResponseEntity.ok(Map.of("suggestions", new ArrayList<>(), "potential_savings", 0));
        }

        System.out.println("AI Savings: Analyzing " + transactions.size() + " transactions");

        String prompt = "Phân tích giao dịch: " + transactions.toString() + 
                        "\nĐưa ra gợi ý tiết kiệm. Trả về JSON: {\"potential_savings\": 1000000, \"monthly_expense\": 5000000, \"savings_rate\": 20, \"overall_recommendation\": [\"...\"], \"suggestions\": [{\"category\": \"...\", \"priority_score\": 5, \"suggestion\": \"...\", \"reasons\": [\"...\"], \"current_spending\": 1000000, \"potential_savings\": 200000, \"avg_amount\": 50000, \"actionable_tips\": [\"...\"]}]}";

        String aiResponse = aiService.generateResponse(prompt);
        System.out.println("AI Savings Response: " + aiResponse);

        try {
            String jsonPart = extractJson(aiResponse);
            Map<String, Object> result = new com.fasterxml.jackson.databind.ObjectMapper().readValue(jsonPart, Map.class);
            
            // Tính toán thêm dữ liệu chi tiêu tháng này cho UI
            Map<String, Object> stats = financeStore.statistics(userId, "all", null, null);
            Map<String, Object> summary = (Map<String, Object>) stats.get("summary");
            result.put("monthly_expense", summary.get("total_expense"));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error parsing AI savings: " + e.getMessage());
            return ResponseEntity.ok(Map.of("suggestions", new ArrayList<>(), "potential_savings", 0));
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        throw new RuntimeException("No JSON found in AI response");
    }
}
