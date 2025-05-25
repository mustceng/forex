package mustceng.forex.service;

import lombok.extern.slf4j.Slf4j;
import mustceng.forex.dto.ConversionRequestDTO;
import mustceng.forex.dto.ConversionResponseDTO;
import mustceng.forex.exception.InvalidInputException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BulkConversionService {

    private final CurrencyConversionService currencyConversionService;

    public BulkConversionService(CurrencyConversionService currencyConversionService) {
        this.currencyConversionService = currencyConversionService;
    }

    /**
     * Processes a bulk CSV file containing currency conversion requests.
     * Each row in the CSV should have: sourceCurrency,targetCurrency,amount
     *
     * @param file The MultipartFile representing the uploaded CSV.
     * @return A list of ConversionResponse objects for successful conversions.
     * @throws InvalidInputException if the file format is incorrect or parsing fails.
     */
    public List<ConversionResponseDTO> processBulkConversionFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidInputException("Uploaded file is empty.");
        }
        if (!"text/csv".equals(file.getContentType())) {
            throw new InvalidInputException("Only CSV files are supported.");
        }

        List<ConversionRequestDTO> conversionRequests = new ArrayList<>();
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());

            for (CSVRecord csvRecord : csvParser) {
                try {
                    String sourceCurrency = csvRecord.get("sourceCurrency");
                    String targetCurrency = csvRecord.get("targetCurrency");
                    BigDecimal amount = new BigDecimal(csvRecord.get("amount"));

                    conversionRequests.add(new ConversionRequestDTO(sourceCurrency, targetCurrency, amount));
                } catch (IllegalArgumentException e) {
                    log.warn("Skipping invalid CSV record: {}. Error: {}", csvRecord.toMap(), e.getMessage());
                    // Optionally, collect errors and return them in the response
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse CSV file: {}", e.getMessage(), e);
            throw new InvalidInputException("Failed to parse CSV file: " + e.getMessage());
        }

        // Process conversions asynchronously to improve responsiveness for bulk operations
        List<CompletableFuture<ConversionResponseDTO>> futures = conversionRequests.stream()
                .map(request -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return currencyConversionService.convertCurrency(request);
                    } catch (Exception e) {
                        log.error("Failed to convert currency for request {}: {}", request, e.getMessage());
                        return null; // Or return an error object if you want to track individual failures
                    }
                }))
                .collect(Collectors.toList());

        // Wait for all futures to complete and collect successful responses
        return futures.stream()
                .map(CompletableFuture::join) // Blocks until the future completes
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }
}
