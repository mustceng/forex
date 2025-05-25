package mustceng.forex.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mustceng.forex.dto.ConversionRequestDTO;
import mustceng.forex.dto.ConversionResponseDTO;
import mustceng.forex.dto.ExchangeRateResponseDTO;
import mustceng.forex.service.CurrencyConversionService;
import mustceng.forex.service.ExchangeRateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/forex")
@Tag(name = "Foreign Exchange Operations", description = "API for fetching exchange rates and performing currency conversions")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;
    private final CurrencyConversionService currencyConversionService;

    public ExchangeRateController(ExchangeRateService exchangeRateService, CurrencyConversionService currencyConversionService) {
        this.exchangeRateService = exchangeRateService;
        this.currencyConversionService = currencyConversionService;
    }

    @Operation(summary = "Get current exchange rate",
            description = "Retrieves the current exchange rate between a source and target currency.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved exchange rate",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExchangeRateResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid currency codes provided",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "503", description = "External exchange rate service unavailable",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/exchange-rate")
    public ResponseEntity<ExchangeRateResponseDTO> getExchangeRate(
            @Parameter(description = "Source currency code (e.g., USD)", required = true, example = "USD")
            @RequestParam String source,
            @Parameter(description = "Target currency code (e.g., EUR)", required = true, example = "EUR")
            @RequestParam String target) {
        ExchangeRateResponseDTO response = exchangeRateService.getExchangeRate(source, target);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Perform currency conversion",
            description = "Converts an amount from a source currency to a target currency and records the transaction.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully converted currency",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConversionResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g., amount less than zero, invalid currency codes)",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "503", description = "External exchange rate service unavailable",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/convert")
    public ResponseEntity<ConversionResponseDTO> convertCurrency(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Conversion request details", required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConversionRequestDTO.class)))
            @Valid @RequestBody ConversionRequestDTO request) {
        ConversionResponseDTO response = currencyConversionService.convertCurrency(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
