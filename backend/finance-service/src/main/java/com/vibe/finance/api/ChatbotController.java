package com.vibe.finance.api;

import com.vibe.finance.service.AIService;
import com.vibe.finance.store.FinanceStore;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chatbot")
public class ChatbotController {
    private final AIService aiService;
    private final FinanceStore financeStore;

    public ChatbotController(AIService aiService, FinanceStore financeStore) {
        this.aiService = aiService;
        this.financeStore = financeStore;
    }

    @PostMapping
    public ResponseEntity<?> chat(
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestBody Map<String, String> payload) {
        System.out.println("[Chatbot] Request from userId: " + userId);
        String message = payload.get("message");
        if (message == null || message.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
        }
        
        if (userId != null) {
            financeStore.saveChatMessage(userId, "user", message);
            System.out.println("[Chatbot] Saved user message for: " + userId);
        }
        
        List<Map<String, Object>> history = (userId != null) ? financeStore.getChatHistory(userId) : null;
        String response = aiService.generateResponse(message, true, history);
        
        if (userId != null) {
            financeStore.saveChatMessage(userId, "bot", response);
            System.out.println("[Chatbot] Saved bot response for: " + userId);
        }
        
        return ResponseEntity.ok(Map.of("response", response));
    }

    @GetMapping("/history")
    public ResponseEntity<?> history(@RequestHeader(name = "X-User-Id", required = false) String userId) {
        System.out.println("[Chatbot] Fetching history for userId: " + userId);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        List<Map<String, Object>> chatHistory = financeStore.getChatHistory(userId);
        System.out.println("[Chatbot] Found " + chatHistory.size() + " messages for " + userId);
        return ResponseEntity.ok(chatHistory);
    }
}
