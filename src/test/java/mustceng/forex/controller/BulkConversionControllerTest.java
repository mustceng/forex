package mustceng.forex.controller;

import mustceng.forex.dto.ConversionResponseDTO;
import mustceng.forex.exception.InvalidInputException;
import mustceng.forex.service.BulkConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BulkConversionController.class)
public class BulkConversionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BulkConversionService bulkConversionService;

    private MockMultipartFile csvFile;
    private List<ConversionResponseDTO> mockResponses;

    @BeforeEach
    void setUp() {
        String csvContent = "sourceCurrency,targetCurrency,amount\nUSD,EUR,100.00\nEUR,GBP,50.00";
        csvFile = new MockMultipartFile(
                "file",
                "conversions.csv",
                "text/csv",
                csvContent.getBytes()
        );

        mockResponses = Arrays.asList(
                new ConversionResponseDTO("id1", "USD", "EUR", new BigDecimal("100.00"), new BigDecimal("92.00"), new BigDecimal("0.92")),
                new ConversionResponseDTO("id2", "EUR", "GBP", new BigDecimal("50.00"), new BigDecimal("43.00"), new BigDecimal("0.86"))
        );
    }

    @Test
    void bulkConvert_success() throws Exception {
        when(bulkConversionService.processBulkConversionFile(any())).thenReturn(mockResponses);

        mockMvc.perform(multipart("/api/v1/forex/bulk-convert")
                        .file(csvFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId").value("id1"))
                .andExpect(jsonPath("$[1].sourceCurrency").value("EUR"));
    }

    @Test
    void bulkConvert_invalidFileFormat() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "invalid.txt",
                "text/plain",
                "not a csv".getBytes()
        );

        when(bulkConversionService.processBulkConversionFile(any()))
                .thenThrow(new InvalidInputException("Only CSV files are supported."));

        mockMvc.perform(multipart("/api/v1/forex/bulk-convert")
                        .file(invalidFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only CSV files are supported."));
    }

    @Test
    void bulkConvert_emptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                new byte[0]
        );

        when(bulkConversionService.processBulkConversionFile(any()))
                .thenThrow(new InvalidInputException("Uploaded file is empty."));

        mockMvc.perform(multipart("/api/v1/forex/bulk-convert")
                        .file(emptyFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Uploaded file is empty."));
    }
}
