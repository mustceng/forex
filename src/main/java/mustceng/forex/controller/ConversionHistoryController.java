package mustceng.forex.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import mustceng.forex.dto.ConversionHistoryResponseDTO;
import mustceng.forex.model.ConversionTransaction;
import mustceng.forex.repository.ConversionTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/forex")
@Tag(name = "Conversion History", description = "API for retrieving currency conversion history")
public class ConversionHistoryController {

    private final ConversionTransactionRepository transactionRepository;

    public ConversionHistoryController(ConversionTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Operation(summary = "Get conversion history",
            description = "Retrieves a paginated list of currency conversions. " +
                    "Filter by transaction ID or transaction date. At least one filter must be provided.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved conversion history",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ConversionHistoryResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid input (no filter provided or invalid date format)",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Transaction not found for the given ID",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/history")
    public ResponseEntity<List<ConversionHistoryResponseDTO>> getConversionHistory(
            @Parameter(description = "Unique transaction identifier (optional)", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
            @RequestParam(required = false) String transactionId,
            @Parameter(description = "Transaction date for filtering (YYYY-MM-DD) (optional)", example = "2023-01-15")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate transactionDate,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        if (transactionId != null && !transactionId.isEmpty()) {
            // Filter by transaction ID
            return transactionRepository.findByTransactionId(transactionId)
                    .map(transaction -> {
                        ConversionHistoryResponseDTO response = mapToHistoryResponse(transaction);
                        return new ResponseEntity<>(List.of(response), HttpStatus.OK);
                    })
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } else if (transactionDate != null) {
            // Filter by transaction date
            LocalDateTime startOfDay = transactionDate.atStartOfDay();
            LocalDateTime endOfDay = transactionDate.atTime(LocalTime.MAX);
            Pageable pageable = PageRequest.of(page, size);
            Page<ConversionTransaction> transactionsPage = transactionRepository.findByTransactionDateBetween(startOfDay, endOfDay, pageable);

            List<ConversionHistoryResponseDTO> responses = transactionsPage.getContent().stream()
                    .map(this::mapToHistoryResponse)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } else {
            // No filter provided
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private ConversionHistoryResponseDTO mapToHistoryResponse(ConversionTransaction transaction) {
        return new ConversionHistoryResponseDTO(
                transaction.getTransactionId(),
                transaction.getSourceCurrency(),
                transaction.getTargetCurrency(),
                transaction.getOriginalAmount(),
                transaction.getConvertedAmount(),
                transaction.getExchangeRate(),
                transaction.getTransactionDate()
        );
    }
}
