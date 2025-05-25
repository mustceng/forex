package mustceng.forex.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import mustceng.forex.dto.ConversionRequestDTO;
import mustceng.forex.dto.ConversionResponseDTO;
import mustceng.forex.dto.ExchangeRateResponseDTO;
import mustceng.forex.exception.ApiIntegrationException;
import mustceng.forex.service.CurrencyConversionService;
import mustceng.forex.service.ExchangeRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExchangeRateController.class)
public class ExchangeRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @MockBean
    private CurrencyConversionService currencyConversionService;

    @Autowired
    private ObjectMapper objectMapper;

    private ExchangeRateResponseDTO exchangeRateResponseDTO;
    private ConversionRequestDTO conversionRequestDTO;
    private ConversionResponseDTO conversionResponseDTO;

    @BeforeEach
    void setUp() {
        exchangeRateResponseDTO = new ExchangeRateResponseDTO("USD", "EUR", new BigDecimal("0.92"));
        conversionRequestDTO = new ConversionRequestDTO("USD", "GBP", new BigDecimal("100.00"));
        conversionResponseDTO = new ConversionResponseDTO("test-id", "USD", "GBP", new BigDecimal("100.00"), new BigDecimal("80.00"), new BigDecimal("0.80"));
    }

    @Test
    void getExchangeRate_success() throws Exception {
        when(exchangeRateService.getExchangeRate(anyString(), anyString())).thenReturn(exchangeRateResponseDTO);

        mockMvc.perform(get("/api/v1/forex/exchange-rate")
                        .param("source", "USD")
                        .param("target", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceCurrency").value("USD"))
                .andExpect(jsonPath("$.targetCurrency").value("EUR"))
                .andExpect(jsonPath("$.rate").value(0.92));
    }

    @Test
    void getExchangeRate_apiIntegrationException() throws Exception {
        when(exchangeRateService.getExchangeRate(anyString(), anyString()))
                .thenThrow(new ApiIntegrationException("API error"));

        mockMvc.perform(get("/api/v1/forex/exchange-rate")
                        .param("source", "USD")
                        .param("target", "EUR"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("API error"));
    }

    @Test
    void convertCurrency_success() throws Exception {
        when(currencyConversionService.convertCurrency(any(ConversionRequestDTO.class))).thenReturn(conversionResponseDTO);

        mockMvc.perform(post("/api/v1/forex/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conversionRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("test-id"))
                .andExpect(jsonPath("$.convertedAmount").value(80.00));
    }

    @Test
    void convertCurrency_invalidInput() throws Exception {
        ConversionRequestDTO invalidRequest = new ConversionRequestDTO("USD", "GBP", new BigDecimal("-10.00")); // Invalid amount

        mockMvc.perform(post("/api/v1/forex/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void convertCurrency_apiIntegrationException() throws Exception {
        when(currencyConversionService.convertCurrency(any(ConversionRequestDTO.class)))
                .thenThrow(new ApiIntegrationException("API error during conversion"));

        mockMvc.perform(post("/api/v1/forex/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conversionRequestDTO)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("API error during conversion"));
    }
}
