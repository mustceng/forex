package mustceng.forex.service;

import mustceng.forex.dto.ConversionRequestDTO;
import mustceng.forex.dto.ConversionResponseDTO;
import mustceng.forex.dto.ExchangeRateResponseDTO;
import mustceng.forex.model.ConversionTransaction;
import mustceng.forex.repository.ConversionTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CurrencyConversionServiceTest {

    @Mock
    private ExchangeRateService exchangeRateService;

    @Mock
    private ConversionTransactionRepository transactionRepository;

    @InjectMocks
    private CurrencyConversionService currencyConversionService;

    private ConversionRequestDTO conversionRequestDTO;
    private ExchangeRateResponseDTO exchangeRateResponseDTO;

    @BeforeEach
    void setUp() {
        conversionRequestDTO = new ConversionRequestDTO("USD", "EUR", new BigDecimal("100.00"));
        exchangeRateResponseDTO = new ExchangeRateResponseDTO("USD", "EUR", new BigDecimal("0.92"));
    }

    @Test
    void convertCurrency_success() {
        // Mock the exchange rate service call
        when(exchangeRateService.getExchangeRate(anyString(), anyString())).thenReturn(exchangeRateResponseDTO);

        // Mock the repository save call
        when(transactionRepository.save(any(ConversionTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0)); // Return the saved object

        ConversionResponseDTO response = currencyConversionService.convertCurrency(conversionRequestDTO);

        assertNotNull(response);
        assertNotNull(response.getTransactionId()); // Should be generated
        assertEquals("USD", response.getSourceCurrency());
        assertEquals("EUR", response.getTargetCurrency());
        assertEquals(new BigDecimal("100.00"), response.getOriginalAmount());
        assertEquals(new BigDecimal("92.0000"), response.getConvertedAmount()); // Scaled to 4 decimal places
        assertEquals(new BigDecimal("0.92"), response.getExchangeRate());

        // Verify that the exchange rate service was called
        verify(exchangeRateService).getExchangeRate("USD", "EUR");

        // Verify that the transaction was saved
        ArgumentCaptor<ConversionTransaction> transactionCaptor = ArgumentCaptor.forClass(ConversionTransaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());

        ConversionTransaction capturedTransaction = transactionCaptor.getValue();
        assertEquals(response.getTransactionId(), capturedTransaction.getTransactionId());
        assertEquals("USD", capturedTransaction.getSourceCurrency());
        assertEquals("EUR", capturedTransaction.getTargetCurrency());
        assertEquals(new BigDecimal("100.00"), capturedTransaction.getOriginalAmount());
        assertEquals(new BigDecimal("92.0000"), capturedTransaction.getConvertedAmount());
        assertEquals(new BigDecimal("0.92"), capturedTransaction.getExchangeRate());
        assertNotNull(capturedTransaction.getTransactionDate());
    }
}
