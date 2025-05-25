package mustceng.forex.service;

import mustceng.forex.dto.ConversionRequestDTO;
import mustceng.forex.dto.ConversionResponseDTO;
import mustceng.forex.exception.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BulkConversionServiceTest {

    @Mock
    private CurrencyConversionService currencyConversionService;

    @InjectMocks
    private BulkConversionService bulkConversionService;

    private MockMultipartFile validCsvFile;
    private MockMultipartFile invalidCsvFile;
    private MockMultipartFile emptyCsvFile;
    private MockMultipartFile nonCsvFile;

    @BeforeEach
    void setUp() {
        String validCsvContent = "sourceCurrency,targetCurrency,amount\nUSD,EUR,100.00\nEUR,GBP,50.00";
        validCsvFile = new MockMultipartFile(
                "file",
                "conversions.csv",
                "text/csv",
                validCsvContent.getBytes()
        );

        String invalidCsvContent = "source,target,amount\nUSD,EUR,abc\n"; // Invalid amount
        invalidCsvFile = new MockMultipartFile(
                "file",
                "invalid.csv",
                "text/csv",
                invalidCsvContent.getBytes()
        );

        emptyCsvFile = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                new byte[0]
        );

        nonCsvFile = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "some text".getBytes()
        );
    }

    @Test
    void processBulkConversionFile_validCsv_success() {
        when(currencyConversionService.convertCurrency(any(ConversionRequestDTO.class)))
                .thenReturn(new ConversionResponseDTO("tx1", "USD", "EUR", new BigDecimal("100.00"), new BigDecimal("92.00"), new BigDecimal("0.92")))
                .thenReturn(new ConversionResponseDTO("tx2", "EUR", "GBP", new BigDecimal("50.00"), new BigDecimal("43.00"), new BigDecimal("0.86")));

        List<ConversionResponseDTO> responses = bulkConversionService.processBulkConversionFile(validCsvFile);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("tx1", responses.get(0).getTransactionId());
        assertEquals("tx2", responses.get(1).getTransactionId());
    }

    @Test
    void processBulkConversionFile_invalidCsvContent_skipsInvalidRecords() {
        when(currencyConversionService.convertCurrency(any(ConversionRequestDTO.class)))
                .thenReturn(new ConversionResponseDTO("tx1", "USD", "EUR", new BigDecimal("100.00"), new BigDecimal("92.00"), new BigDecimal("0.92")));

        // This CSV has one valid and one invalid record
        String mixedCsvContent = "sourceCurrency,targetCurrency,amount\nUSD,EUR,100.00\nEUR,GBP,invalid_amount";
        MockMultipartFile mixedCsvFile = new MockMultipartFile(
                "file",
                "mixed.csv",
                "text/csv",
                mixedCsvContent.getBytes()
        );

        List<ConversionResponseDTO> responses = bulkConversionService.processBulkConversionFile(mixedCsvFile);

        assertNotNull(responses);
        assertEquals(1, responses.size()); // Only the valid record should be processed
        assertEquals("tx1", responses.get(0).getTransactionId());
    }

    @Test
    void processBulkConversionFile_emptyFile_throwsInvalidInputException() {
        InvalidInputException thrown = assertThrows(InvalidInputException.class, () -> {
            bulkConversionService.processBulkConversionFile(emptyCsvFile);
        });
        assertTrue(thrown.getMessage().contains("Uploaded file is empty."));
    }

    @Test
    void processBulkConversionFile_nonCsvFile_throwsInvalidInputException() {
        InvalidInputException thrown = assertThrows(InvalidInputException.class, () -> {
            bulkConversionService.processBulkConversionFile(nonCsvFile);
        });
        assertTrue(thrown.getMessage().contains("Only CSV files are supported."));
    }

    @Test
    void processBulkConversionFile_parsingError_throwsInvalidInputException() {
        // Simulate a CSV with missing headers or other parsing issues
        String malformedCsvContent = "USD,EUR,100.00\n"; // Missing header
        MockMultipartFile malformedCsvFile = new MockMultipartFile(
                "file",
                "malformed.csv",
                "text/csv",
                malformedCsvContent.getBytes()
        );

        InvalidInputException thrown = assertThrows(InvalidInputException.class, () -> {
            bulkConversionService.processBulkConversionFile(malformedCsvFile);
        });
        assertTrue(thrown.getMessage().contains("Failed to parse CSV file"));
    }
}
