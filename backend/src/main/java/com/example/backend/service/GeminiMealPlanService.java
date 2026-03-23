package com.example.backend.service;

import com.example.backend.dto.MealPlanDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class GeminiMealPlanService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private final String apiKey;
    private final String modelName;

    public GeminiMealPlanService(
            ObjectMapper objectMapper,
            @Value("${gemini.api.key:}") String apiKey,
            @Value("${gemini.model.name:gemini-2.5-flash}") String modelName
    ) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.apiKey = apiKey;
        this.modelName = modelName;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public GeminiMealPlanResult generateMealPlan(
            String gender,
            int age,
            double heightCm,
            double currentWeightKg,
            double targetWeightKg,
            String goalTypeRaw,
            long numberOfDays,
            java.time.LocalDate startDate,
            double caloriesPerDay
    ) throws Exception {
        if (!isConfigured()) {
            throw new IllegalStateException("Chưa cấu hình gemini.api.key (GEMINI_AI_KEY).");
        }

        String prompt = buildPrompt(
                gender,
                age,
                heightCm,
                currentWeightKg,
                targetWeightKg,
                goalTypeRaw,
                numberOfDays,
                startDate,
                caloriesPerDay
        );

        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;

        // Build request via Map for readability.
        var requestMap = java.util.Map.of(
                "contents", java.util.List.of(
                        java.util.Map.of(
                                "role", "user",
                                "parts", java.util.List.of(
                                        java.util.Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", java.util.Map.of(
                        "temperature", 0.4,
                        "responseMimeType", "application/json"
                )
        );

        String requestJson = objectMapper.writeValueAsString(requestMap);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Gemini HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        // candidates[0].content.parts[0].text
        JsonNode textNode = root.path("candidates").isArray() && root.path("candidates").size() > 0
                ? root.path("candidates").get(0).path("content").path("parts").get(0).path("text")
                : null;

        if (textNode == null || textNode.isMissingNode()) {
            throw new IllegalStateException("Không tìm thấy text response từ Gemini.");
        }

        String rawText = textNode.asText();
        String extractedJson = extractFirstJsonObject(rawText);
        MealPlanDto plan = objectMapper.readValue(extractedJson, MealPlanDto.class);
        return new GeminiMealPlanResult(plan, rawText);
    }

    private String buildPrompt(
            String gender,
            int age,
            double heightCm,
            double currentWeightKg,
            double targetWeightKg,
            String goalTypeRaw,
            long numberOfDays,
            java.time.LocalDate startDate,
            double caloriesPerDay
    ) {
        // Nếu LLM không khớp đúng toàn bộ schema thì parser sẽ fail và NutritionService sẽ fallback heuristic.
        return "Bạn là chuyên gia dinh dưỡng.\n" +
                "Hãy tạo lộ trình ăn uống cho người dùng trong " + numberOfDays + " ngày.\n" +
                "Mỗi ngày chia 3 bữa: SANG, TRUA, TOI.\n" +
                "Mục tiêu năng lượng: " + caloriesPerDay + " kcal/ngày. Tổng calories của 3 bữa mỗi ngày = caloriesPerDay (làm tròn gần nhất).\n" +
                "Thông tin người dùng: giới tính=" + gender + ", tuổi=" + age + ", chiều cao=" + heightCm + " cm, cân nặng hiện tại=" + currentWeightKg + " kg, cân nặng mục tiêu=" + targetWeightKg + " kg, goalType=" + goalTypeRaw + ".\n\n" +
                "Trả về CHỈ DUY NHẤT một JSON hợp lệ, KHÔNG dùng markdown.\n" +
                "Schema JSON:\n" +
                "{\n" +
                "  \"planTitle\": string,\n" +
                "  \"caloriesPerDay\": number,\n" +
                "  \"days\": [\n" +
                "    {\n" +
                "      \"dayIndex\": number,\n" +
                "      \"date\": string (yyyy-MM-dd),\n" +
                "      \"meals\": [\n" +
                "        {\"mealType\":\"SANG\",\"name\":string,\"description\":string,\"caloriesEstimated\":number},\n" +
                "        {\"mealType\":\"TRUA\",\"name\":string,\"description\":string,\"caloriesEstimated\":number},\n" +
                "        {\"mealType\":\"TOI\",\"name\":string,\"description\":string,\"caloriesEstimated\":number}\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n" +
                "Bắt đầu ngày đầu tiên từ: " + startDate.toString() + ".\n" +
                "dayIndex bắt đầu từ 1 và tăng dần theo ngày.\n";
    }

    private String extractFirstJsonObject(String text) {
        // Gemini đôi khi trả kèm giải thích; cố gắng lấy phần JSON bao ngoài cùng.
        int firstBrace = text.indexOf('{');
        int lastBrace = text.lastIndexOf('}');
        if (firstBrace < 0 || lastBrace <= firstBrace) {
            return text; // fallback: để parser tự báo lỗi
        }
        return text.substring(firstBrace, lastBrace + 1);
    }

    public record GeminiMealPlanResult(MealPlanDto plan, String rawText) {
    }
}

