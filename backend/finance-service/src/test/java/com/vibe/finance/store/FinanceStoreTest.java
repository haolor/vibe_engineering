package com.vibe.finance.store;

import com.vibe.finance.config.SequenceService;
import com.vibe.finance.model.CategoryDocument;
import com.vibe.finance.model.TransactionDocument;
import com.vibe.finance.model.ChatMessageDocument;
import com.vibe.finance.repo.CategoryRepository;
import com.vibe.finance.repo.TransactionRepository;
import com.vibe.finance.repo.ChatMessageRepository;
import com.vibe.finance.service.NotificationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FinanceStoreTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private SequenceService sequenceService;

    @Mock
    private NotificationClient notificationClient;

    private FinanceStore financeStore;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Mock default behavior for seedCategoriesIfEmpty called in constructor
        when(categoryRepository.findAll()).thenReturn(new ArrayList<>());
        financeStore = new FinanceStore(categoryRepository, transactionRepository, chatMessageRepository, sequenceService, notificationClient);
    }

    @Test
    public void testCreateCategory() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Cafe");
        payload.put("type", "expense");
        payload.put("icon", "☕");
        payload.put("color", "#ff4444");

        when(sequenceService.next("category_id")).thenReturn(12L);
        when(categoryRepository.save(any(CategoryDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = financeStore.createCategory(payload);

        assertNotNull(result);
        assertEquals(12L, result.get("id"));
        assertEquals("Cafe", result.get("name"));
        assertEquals("expense", result.get("type"));
        assertEquals("☕", result.get("icon"));
        assertEquals("#ff4444", result.get("color"));
        verify(categoryRepository, times(1)).save(argThat(c -> c != null && "Cafe".equals(c.getName())));
    }

    @Test
    public void testCategories() {
        CategoryDocument cat = new CategoryDocument();
        cat.setId(1L);
        cat.setName("Salary");
        cat.setType("income");
        cat.setIcon("💼");
        cat.setColor("#22c55e");

        when(categoryRepository.findAll()).thenReturn(List.of(cat));

        List<Map<String, Object>> result = financeStore.categories();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).get("id"));
        assertEquals("Salary", result.get(0).get("name"));
    }

    @Test
    public void testListTransactions() {
        TransactionDocument tx = new TransactionDocument();
        tx.setId(101L);
        tx.setUserId("user1");
        tx.setAmount(100.0);
        tx.setTransactionDate("2026-05-19");
        tx.setCategoryName("Cafe");

        when(transactionRepository.findByUserIdOrderByTransactionDateDescIdDesc("user1"))
                .thenReturn(List.of(tx));

        Map<String, Object> response = financeStore.listTransactions("user1", 1);

        assertNotNull(response);
        assertEquals(1, response.get("count"));
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
        assertEquals(1, results.size());
        assertEquals(101L, results.get(0).get("id"));
    }

    @Test
    public void testCreateTransaction() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("category", "1");
        payload.put("amount", "50000");
        payload.put("description", "Bonus");
        payload.put("transaction_date", "2026-05-19");

        CategoryDocument cat = new CategoryDocument();
        cat.setId(1L);
        cat.setName("Salary");
        cat.setType("income");
        cat.setIcon("💼");
        cat.setColor("#22c55e");

        when(categoryRepository.findAll()).thenReturn(List.of(cat));
        when(sequenceService.next("transaction_id")).thenReturn(100L);
        when(transactionRepository.save(any(TransactionDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = financeStore.createTransaction("user1", payload);

        assertNotNull(result);
        assertEquals(100L, result.get("id"));
        assertEquals(50000.0, result.get("amount"));
        assertEquals("Bonus", result.get("description"));
        assertEquals("Salary", result.get("category_name"));
        assertEquals("income", result.get("category_type"));

        verify(notificationClient, times(1)).checkAnomaly(eq("user1"), anyMap());
    }

    @Test
    public void testUpdateTransaction() {
        TransactionDocument existingTx = new TransactionDocument();
        existingTx.setId(200L);
        existingTx.setUserId("user1");
        existingTx.setAmount(10000.0);
        existingTx.setDescription("Old lunch");
        existingTx.setCategory(2L);

        CategoryDocument cat = new CategoryDocument();
        cat.setId(2L);
        cat.setName("An uong");
        cat.setType("expense");

        when(transactionRepository.findByUserIdAndId("user1", 200L)).thenReturn(Optional.of(existingTx));
        when(categoryRepository.findAll()).thenReturn(List.of(cat));
        when(transactionRepository.save(any(TransactionDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> payload = new HashMap<>();
        payload.put("category", "2");
        payload.put("amount", "15000.0");
        payload.put("description", "New lunch");
        payload.put("transaction_date", "2026-05-19");

        Map<String, Object> result = financeStore.updateTransaction("user1", 200L, payload);

        assertNotNull(result);
        assertEquals(200L, result.get("id"));
        assertEquals(15000.0, result.get("amount"));
        assertEquals("New lunch", result.get("description"));
        assertEquals("An uong", result.get("category_name"));

        verify(notificationClient, times(1)).checkAnomaly(eq("user1"), anyMap());
    }

    @Test
    public void testDeleteTransaction() {
        TransactionDocument tx = new TransactionDocument();
        tx.setId(300L);
        tx.setUserId("user1");

        when(transactionRepository.findByUserIdAndId("user1", 300L)).thenReturn(Optional.of(tx));

        financeStore.deleteTransaction("user1", 300L);

        verify(transactionRepository, times(1)).delete(tx);
    }

    @Test
    public void testStatistics() {
        TransactionDocument tx1 = new TransactionDocument();
        tx1.setId(1L);
        tx1.setAmount(5000.0);
        tx1.setCategoryType("income");
        tx1.setCategoryName("Salary");
        tx1.setTransactionDate("2026-05-18");

        TransactionDocument tx2 = new TransactionDocument();
        tx2.setId(2L);
        tx2.setAmount(2000.0);
        tx2.setCategoryType("expense");
        tx2.setCategoryName("An uong");
        tx2.setTransactionDate("2026-05-19");
        tx2.setCategory(2L);

        when(transactionRepository.findByUserIdOrderByTransactionDateDescIdDesc("user1"))
                .thenReturn(List.of(tx1, tx2));

        Map<String, Object> stats = financeStore.statistics("user1", "all", "2026-05-18", "2026-05-19", null);

        assertNotNull(stats);
        Map<String, Object> summary = (Map<String, Object>) stats.get("summary");
        assertEquals(5000.0, summary.get("total_income"));
        assertEquals(2000.0, summary.get("total_expense"));
        assertEquals(3000.0, summary.get("balance"));

        List<Map<String, Object>> byCategory = (List<Map<String, Object>>) stats.get("by_category");
        assertEquals(2, byCategory.size());
    }

    @Test
    public void testChatMessageHistory() {
        financeStore.saveChatMessage("user1", "user", "Hello bot");
        verify(chatMessageRepository, times(1)).save(any(ChatMessageDocument.class));
    }
}
