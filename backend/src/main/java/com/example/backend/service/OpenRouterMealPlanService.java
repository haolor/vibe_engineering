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
public class OpenRouterMealPlanService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private final String apiKey;
    private final String modelName;
    private final String baseUrl;

    public OpenRouterMealPlanService(
            ObjectMapper objectMapper,
            @Value("${openrouter.api.key:}") String apiKey,
            @Value("${openrouter.model.name:openai/gpt-oss-120b}") String modelName,
            @Value("${openrouter.base.url:https://openrouter.ai/api/v1}") String baseUrl
    ) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.baseUrl = baseUrl;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public OpenRouterMealPlanResult generateMealPlan(
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
            throw new IllegalStateException("Chưa cấu hình openrouter.api.key (OPENROUTER_API_KEY).");
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

        // OpenRouter supports OpenAI-compatible APIs.
        // Docs: https://openrouter.ai/docs
        String url = baseUrl + "/chat/completions";

        var requestMap = java.util.Map.of(
                "model", modelName,
                "temperature", 0.4,
                "messages", java.util.List.of(
                        java.util.Map.of(
                                "role", "user",
                                "content", prompt
                        )
                )
        );

        String requestJson = objectMapper.writeValueAsString(requestMap);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("OpenRouter HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());

        // OpenAI-compatible: choices[0].message.content
        JsonNode contentNode = root.path("choices").isArray() && root.path("choices").size() > 0
                ? root.path("choices").get(0).path("message").path("content")
                : null;

        if (contentNode == null || contentNode.isMissingNode()) {
            throw new IllegalStateException("Không tìm thấy content response từ OpenRouter.");
        }

        String rawText = contentNode.asText();
        String extractedJson = extractFirstJsonObject(rawText);
        MealPlanDto plan = objectMapper.readValue(extractedJson, MealPlanDto.class);
        return new OpenRouterMealPlanResult(plan, rawText);
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
        // OpenRouter có thể trả kèm giải thích; cố gắng lấy phần JSON bao ngoài cùng.
        int firstBrace = text.indexOf('{');
        int lastBrace = text.lastIndexOf('}');
        if (firstBrace < 0 || lastBrace <= firstBrace) {
            return text; // fallback: để parser tự báo lỗi
        }
        return text.substring(firstBrace, lastBrace + 1);
    }

    public record OpenRouterMealPlanResult(MealPlanDto plan, String rawText) {
    }
}

