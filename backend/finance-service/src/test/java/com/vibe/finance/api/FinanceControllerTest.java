package com.vibe.finance.api;

import com.vibe.finance.service.AIService;
import com.vibe.finance.store.FinanceStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FinanceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FinanceStore financeStore;

    @Mock
    private AIService aiService;

    private FinanceController financeController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        financeController = new FinanceController(financeStore, aiService);
        mockMvc = MockMvcBuilders.standaloneSetup(financeController).build();
    }

    @Test
    public void testGetCategories() throws Exception {
        when(financeStore.categories()).thenReturn(List.of(Map.of("id", 1L, "name", "An uong")));

        mockMvc.perform(get("/categories/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("An uong"));
    }

    @Test
    public void testCreateCategory() throws Exception {
        when(financeStore.createCategory(anyMap())).thenReturn(Map.of("id", 1L, "name", "An uong"));

        mockMvc.perform(post("/categories/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"An uong\", \"type\": \"expense\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("An uong"));
    }

    @Test
    public void testListTransactionsUnauthorized() throws Exception {
        mockMvc.perform(get("/transactions/"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testListTransactions() throws Exception {
        when(financeStore.listTransactions("user1", 1)).thenReturn(Map.of("count", 1, "results", List.of()));

        mockMvc.perform(get("/transactions/")
                        .header("X-User-Id", "user1")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    public void testCreateTransaction() throws Exception {
        Map<String, Object> expected = Map.of("id", 1L, "amount", 5000.0);
        when(financeStore.createTransaction(eq("user1"), anyMap())).thenReturn(expected);

        mockMvc.perform(post("/transactions/")
                        .header("X-User-Id", "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 5000.0, \"category\": \"1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(5000.0));
    }

    @Test
    public void testDeleteTransaction() throws Exception {
        mockMvc.perform(delete("/transactions/1/")
                        .header("X-User-Id", "user1"))
                .andExpect(status().isNoContent());

        verify(financeStore, times(1)).deleteTransaction("user1", 1L);
    }

    @Test
    public void testNlpQuery() throws Exception {
        when(financeStore.recentExpenses("user1")).thenReturn(List.of());
        when(aiService.generateResponse(anyString(), eq(true))).thenReturn("Hello I am your AI assistant");

        mockMvc.perform(post("/transactions/nlp_query/")
                        .header("X-User-Id", "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Hello I am your AI assistant"));
    }
}
