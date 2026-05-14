package com.vibe.finance.store;

import com.vibe.finance.config.SequenceService;
import com.vibe.finance.model.CategoryDocument;
import com.vibe.finance.model.TransactionDocument;
import com.vibe.finance.model.ChatMessageDocument;
import com.vibe.finance.repo.CategoryRepository;
import com.vibe.finance.repo.TransactionRepository;
import com.vibe.finance.repo.ChatMessageRepository;
import com.vibe.finance.service.NotificationClient;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class FinanceStore {
    private static final int PAGE_SIZE = 20;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SequenceService sequenceService;
    private final NotificationClient notificationClient;

    public FinanceStore(
            CategoryRepository categoryRepository,
            TransactionRepository transactionRepository,
            ChatMessageRepository chatMessageRepository,
            SequenceService sequenceService,
            NotificationClient notificationClient) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.sequenceService = sequenceService;
        this.notificationClient = notificationClient;
        seedCategoriesIfEmpty();
    }

    public Map<String, Object> createCategory(Map<String, Object> payload) {
        CategoryDocument cat = new CategoryDocument();
        cat.setId(sequenceService.next("category_id"));
        cat.setName(String.valueOf(payload.get("name")));
        cat.setType(String.valueOf(payload.getOrDefault("type", "expense")));
        cat.setIcon(String.valueOf(payload.getOrDefault("icon", "📦")));
        cat.setColor(String.valueOf(payload.getOrDefault("color", "#6b7280")));
        return categoryMap(categoryRepository.save(cat));
    }

    public List<Map<String, Object>> categories() {
        return categoryRepository.findAll().stream().map(this::categoryMap).toList();
    }

    public Map<String, Object> listTransactions(String userId, int page) {
        List<Map<String, Object>> all = transactionRepository.findByUserIdOrderByTransactionDateDescIdDesc(userId)
                .stream().map(this::transactionMap).toList();
        int total = all.size();
        int from = Math.max(0, (page - 1) * PAGE_SIZE);
        int to = Math.min(total, from + PAGE_SIZE);
        List<Map<String, Object>> results = from >= to ? List.of() : new ArrayList<>(all.subList(from, to));
        Map<String, Object> response = new HashMap<>();
        response.put("count", total);
        response.put("next", to < total ? "/api/transactions/?page=" + (page + 1) : null);
        response.put("previous", page > 1 ? "/api/transactions/?page=" + (page - 1) : null);
        response.put("results", results);
        return response;
    }

    public Map<String, Object> createTransaction(String userId, Map<String, Object> payload) {
        CategoryDocument category = findCategory(Long.parseLong(String.valueOf(payload.get("category"))));
        TransactionDocument tx = new TransactionDocument();
        tx.setId(sequenceService.next("transaction_id"));
        tx.setUserId(userId);
        tx.setAmount(Double.parseDouble(String.valueOf(payload.get("amount"))));
        tx.setDescription(String.valueOf(payload.getOrDefault("description", "")));
        tx.setCategory(category.getId());
        tx.setTransactionDate(String.valueOf(payload.get("transaction_date")));
        tx.setCategoryName(category.getName());
        tx.setCategoryIcon(category.getIcon());
        tx.setCategoryColor(category.getColor());
        tx.setCategoryType(category.getType());
        Map<String, Object> result = transactionMap(transactionRepository.save(tx));
        notificationClient.checkAnomaly(userId, result);
        return result;
    }

    public Map<String, Object> updateTransaction(String userId, long id, Map<String, Object> payload) {
        TransactionDocument tx = transactionRepository.findByUserIdAndId(userId, id)
                .orElseThrow(() -> new IllegalArgumentException("transaction"));
        CategoryDocument category = findCategory(Long.parseLong(String.valueOf(payload.get("category"))));
        tx.setAmount(Double.parseDouble(String.valueOf(payload.get("amount"))));
        tx.setDescription(String.valueOf(payload.getOrDefault("description", "")));
        tx.setCategory(category.getId());
        tx.setTransactionDate(String.valueOf(payload.get("transaction_date")));
        tx.setCategoryName(category.getName());
        tx.setCategoryIcon(category.getIcon());
        tx.setCategoryColor(category.getColor());
        tx.setCategoryType(category.getType());
        Map<String, Object> result = transactionMap(transactionRepository.save(tx));
        notificationClient.checkAnomaly(userId, result);
        return result;
    }

    public void deleteTransaction(String userId, long id) {
        transactionRepository.findByUserIdAndId(userId, id).ifPresent(transactionRepository::delete);
    }

    public Map<String, Object> statistics(String userId, String period, String startDate, String endDate, List<Long> categoryIds) {
        List<Map<String, Object>> all = transactionRepository.findByUserIdOrderByTransactionDateDescIdDesc(userId)
                .stream().map(this::transactionMap).toList();
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.MIN;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.MAX;
        
        // Nếu không truyền ngày nhưng có period (đây là logic cũ, ta giữ lại nếu cần)
        if (period != null && !"all".equals(period) && startDate == null) {
            start = LocalDate.now().minusMonths(1);
            end = LocalDate.now();
        }

        double income = 0;
        double expense = 0;
        Map<String, Map<String, Object>> byDate = new HashMap<>();
        Map<String, Map<String, Object>> byCategory = new HashMap<>();

        for (Map<String, Object> tx : all) {
            LocalDate date = LocalDate.parse(String.valueOf(tx.get("transaction_date")));
            if (date.isBefore(start) || date.isAfter(end)) {
                continue;
            }
            
            // Lọc theo danh mục nếu có
            if (categoryIds != null && !categoryIds.isEmpty()) {
                Long catId = ((Number) tx.get("category")).longValue();
                if (!categoryIds.contains(catId)) {
                    continue;
                }
            }
            double amount = ((Number) tx.get("amount")).doubleValue();
            String type = String.valueOf(tx.get("category_type"));
            if ("income".equals(type)) {
                income += amount;
            } else {
                expense += amount;
            }

            Map<String, Object> daily = byDate.computeIfAbsent(String.valueOf(tx.get("transaction_date")), key -> {
                Map<String, Object> value = new HashMap<>();
                value.put("date", key);
                value.put("income", 0.0);
                value.put("expense", 0.0);
                return value;
            });
            daily.put(type, ((Number) daily.get(type)).doubleValue() + amount);

            String cat = String.valueOf(tx.get("category_name"));
            Map<String, Object> catAgg = byCategory.computeIfAbsent(cat, key -> {
                Map<String, Object> value = new HashMap<>();
                value.put("category__name", key);
                value.put("category__type", type);
                value.put("total", 0.0);
                value.put("count", 0);
                return value;
            });
            catAgg.put("total", ((Number) catAgg.get("total")).doubleValue() + amount);
            catAgg.put("count", ((Number) catAgg.get("count")).intValue() + 1);
        }

        return Map.of(
                "summary", Map.of(
                        "total_income", income,
                        "total_expense", expense,
                        "balance", income - expense
                ),
                "by_date", new ArrayList<>(byDate.values()),
                "by_category", new ArrayList<>(byCategory.values())
        );
    }

    public List<Map<String, Object>> getAllTransactions(String userId) {
        return transactionRepository.findByUserIdOrderByTransactionDateDescIdDesc(userId)
                .stream().map(this::transactionMap).toList();
    }

    public List<Map<String, Object>> recentExpenses(String userId) {
        return transactionRepository.findByUserIdOrderByTransactionDateDescIdDesc(userId).stream()
                .filter(item -> "expense".equals(item.getCategoryType()))
                .limit(5)
                .map(this::transactionMap)
                .toList();
    }

    public List<Map<String, Object>> getChatHistory(String userId) {
        return chatMessageRepository.findByUserIdOrderByTimestampAsc(userId).stream()
                .map(msg -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("type", msg.getRole());
                    map.put("text", msg.getContent());
                    map.put("timestamp", msg.getTimestamp());
                    return map;
                }).toList();
    }

    public void saveChatMessage(String userId, String role, String content) {
        ChatMessageDocument msg = new ChatMessageDocument();
        msg.setUserId(userId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setTimestamp(LocalDateTime.now());
        chatMessageRepository.save(msg);
    }

    private CategoryDocument findCategory(long id) {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getId() != null && c.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("category not found: " + id));
    }

    private void seedCategoriesIfEmpty() {
        Set<Long> existingIds = categoryRepository.findAll().stream()
                .map(CategoryDocument::getId)
                .filter(i -> i != null)
                .collect(Collectors.toSet());
        if (!existingIds.contains(1L)) categoryRepository.save(category(1L, "Luong", "income", "💼", "#22c55e"));
        if (!existingIds.contains(2L)) categoryRepository.save(category(2L, "An uong", "expense", "🍜", "#ef4444"));
        if (!existingIds.contains(3L)) categoryRepository.save(category(3L, "Di chuyen", "expense", "🚗", "#f97316"));
        if (!existingIds.contains(4L)) categoryRepository.save(category(4L, "Thuong", "income", "🎁", "#10b981"));
        if (!existingIds.contains(5L)) categoryRepository.save(category(5L, "Khac", "expense", "📦", "#6b7280"));
        // Ensure the counter starts AFTER the seeded IDs so new categories never collide
        sequenceService.ensureMinimum("category_id", 11L);
    }

    private CategoryDocument category(long id, String name, String type, String icon, String color) {
        CategoryDocument cat = new CategoryDocument();
        cat.setId(id);
        cat.setName(name);
        cat.setType(type);
        cat.setIcon(icon);
        cat.setColor(color);
        return cat;
    }

    private Map<String, Object> categoryMap(CategoryDocument category) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", category.getId());
        map.put("name", category.getName());
        map.put("type", category.getType());
        map.put("icon", category.getIcon());
        map.put("color", category.getColor());
        return map;
    }

    private Map<String, Object> transactionMap(TransactionDocument tx) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", tx.getId());
        map.put("amount", tx.getAmount());
        map.put("description", tx.getDescription());
        map.put("category", tx.getCategory());
        map.put("transaction_date", tx.getTransactionDate());
        map.put("category_name", tx.getCategoryName());
        map.put("category_icon", tx.getCategoryIcon());
        map.put("category_color", tx.getCategoryColor());
        map.put("category_type", tx.getCategoryType());
        return map;
    }
}
