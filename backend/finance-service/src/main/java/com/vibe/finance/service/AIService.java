package com.vibe.finance.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AIService {
    private final WebClient webClient;
    
    @Value("${app.groq.api-key}")
    private String apiKey;
    
    @Value("${app.groq.model-realtime}") 
    private String modelRealtime;

    @Value("${app.groq.model-analytics}") 
    private String modelAnalytics;

    public AIService(@org.springframework.beans.factory.annotation.Qualifier("externalWebClientBuilder") WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Default response using Analytics model (Llama 3.3 70B)
     */
    public String generateResponse(String prompt) {
        return generateResponse(prompt, false, null);
    }

    /**
     * Generate response with specific model type
     * @param prompt The user prompt
     * @param isRealtime If true, use compound-mini for low latency. If false, use llama-3.3-70b.
     */
    public String generateResponse(String prompt, boolean isRealtime) {
        return generateResponse(prompt, isRealtime, null);
    }

    /**
     * Generate response with specific model type and history
     * @param prompt The user prompt
     * @param isRealtime If true, use compound-mini for low latency. If false, use llama-3.3-70b.
     * @param history List of previous messages
     */
    public String generateResponse(String prompt, boolean isRealtime, List<Map<String, Object>> history) {
        String url = "https://api.groq.com/openai/v1/chat/completions";
        String selectedModel = isRealtime ? modelRealtime : modelAnalytics;
        
        java.util.ArrayList<Map<String, Object>> messages = new java.util.ArrayList<>();
        
        // Add history context (limit to last 10 messages to avoid token bloat)
        if (history != null && !history.isEmpty()) {
            int start = Math.max(0, history.size() - 10);
            for (int i = start; i < history.size(); i++) {
                Map<String, Object> msg = history.get(i);
                String role = "bot".equals(msg.get("type")) ? "assistant" : "user";
                messages.add(Map.of("role", role, "content", String.valueOf(msg.get("text"))));
            }
        }
        
        // Add current prompt
        messages.add(Map.of("role", "user", "content", prompt));
        
        Map<String, Object> body = Map.of(
            "model", selectedModel,
            "messages", messages,
            "max_tokens", isRealtime ? 1000 : 2000
        );

        try {
            Map<String, Object> response = webClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.isError(), clientResponse -> 
                    clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                        System.err.println("Groq API Error Body: " + errorBody);
                        return Mono.error(new RuntimeException("API Error: " + errorBody));
                    })
                )
                .bodyToMono(Map.class)
                .block();

            if (response != null && response.containsKey("choices")) {
                // Log token usage
                if (response.containsKey("usage")) {
                    Map<String, Object> usage = (Map<String, Object>) response.get("usage");
                    int promptTokens = ((Number) usage.getOrDefault("prompt_tokens", 0)).intValue();
                    int completionTokens = ((Number) usage.getOrDefault("completion_tokens", 0)).intValue();
                    int totalTokens = ((Number) usage.getOrDefault("total_tokens", 0)).intValue();
                    
                    System.out.println("[Groq Token Usage - Model: " + selectedModel + "]");
                    System.out.println(" - Prompt: " + promptTokens);
                    System.out.println(" - Completion: " + completionTokens);
                    System.out.println(" - Total: " + totalTokens);
                }
                
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) choice.get("message");
                    return (String) message.get("content");
                }
            }
            return "Xin lỗi, tôi không thể trả lời lúc này.";
        } catch (Exception e) {
            return "Lỗi khi kết nối với AI (Groq): " + e.getMessage();
        }
    }
}
