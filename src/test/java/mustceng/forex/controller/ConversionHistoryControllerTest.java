package mustceng.forex.controller;

import mustceng.forex.model.ConversionTransaction;
import mustceng.forex.repository.ConversionTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConversionHistoryController.class)
public class ConversionHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConversionTransactionRepository transactionRepository;

    private ConversionTransaction transaction1;
    private ConversionTransaction transaction2;

    @BeforeEach
    void setUp() {
        transaction1 = new ConversionTransaction(
                1L, "tx123", "USD", "EUR", new BigDecimal("100.00"), new BigDecimal("92.00"), new BigDecimal("0.92"), LocalDateTime.of(2023, 1, 15, 10, 0)
        );
        transaction2 = new ConversionTransaction(
                2L, "tx456", "EUR", "GBP", new BigDecimal("50.00"), new BigDecimal("43.00"), new BigDecimal("0.86"), LocalDateTime.of(2023, 1, 15, 11, 0)
        );
    }

    @Test
    void getConversionHistory_byTransactionId_success() throws Exception {
        when(transactionRepository.findByTransactionId(anyString())).thenReturn(Optional.of(transaction1));

        mockMvc.perform(get("/api/v1/forex/history")
                        .param("transactionId", "tx123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId").value("tx123"))
                .andExpect(jsonPath("$[0].sourceCurrency").value("USD"));
    }

    @Test
    void getConversionHistory_byTransactionId_notFound() throws Exception {
        when(transactionRepository.findByTransactionId(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/forex/history")
                        .param("transactionId", "nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getConversionHistory_byTransactionDate_success() throws Exception {
        when(transactionRepository.findByTransactionDateBetween(any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(transaction1, transaction2)));

        mockMvc.perform(get("/api/v1/forex/history")
                        .param("transactionDate", "2023-01-15")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId").value("tx123"))
                .andExpect(jsonPath("$[1].transactionId").value("tx456"));
    }

    @Test
    void getConversionHistory_noFilter_badRequest() throws Exception {
        mockMvc.perform(get("/api/v1/forex/history"))
                .andExpect(status().isBadRequest());
    }
}
