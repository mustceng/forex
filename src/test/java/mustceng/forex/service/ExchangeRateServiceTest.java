package mustceng.forex.service;

import mustceng.forex.dto.ExchangeRateResponseDTO;
import mustceng.forex.exception.ApiIntegrationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateApiClient exchangeRateApiClient;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    private BigDecimal mockRate;

    @BeforeEach
    void setUp() {
        mockRate = new BigDecimal("0.92");
    }

    @Test
    void getExchangeRate_success() {
        when(exchangeRateApiClient.getExchangeRate(anyString(), anyString())).thenReturn(mockRate);

        ExchangeRateResponseDTO response = exchangeRateService.getExchangeRate("USD", "EUR");

        assertNotNull(response);
        assertEquals("USD", response.getSourceCurrency());
        assertEquals("EUR", response.getTargetCurrency());
        assertEquals(mockRate, response.getRate());
    }

    @Test
    void getExchangeRate_apiIntegrationException() {
        when(exchangeRateApiClient.getExchangeRate(anyString(), anyString()))
                .thenThrow(new ApiIntegrationException("External API call failed"));

        ApiIntegrationException thrown = assertThrows(ApiIntegrationException.class, () -> {
            exchangeRateService.getExchangeRate("USD", "EUR");
        });

        assertTrue(thrown.getMessage().contains("External API call failed"));
    }

    @Test
    void getExchangeRate_currencyCodesConvertedToUpperCase() {
        when(exchangeRateApiClient.getExchangeRate("USD", "EUR")).thenReturn(mockRate);

        ExchangeRateResponseDTO response = exchangeRateService.getExchangeRate("usd", "eur");

        assertNotNull(response);
        assertEquals("USD", response.getSourceCurrency());
        assertEquals("EUR", response.getTargetCurrency());
        assertEquals(mockRate, response.getRate());
    }
}
