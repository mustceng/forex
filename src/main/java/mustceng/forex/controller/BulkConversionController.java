package mustceng.forex.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import mustceng.forex.dto.ConversionResponseDTO;
import mustceng.forex.service.BulkConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/forex")
@Tag(name = "Bulk Operations", description = "API for processing bulk currency conversion requests via file upload")
public class BulkConversionController {

    private final BulkConversionService bulkConversionService;

    public BulkConversionController(BulkConversionService bulkConversionService) {
        this.bulkConversionService = bulkConversionService;
    }

    @Operation(summary = "Process bulk currency conversion file",
            description = "Uploads a CSV file containing multiple currency conversion requests and processes them. " +
                    "The CSV file should have headers: sourceCurrency,targetCurrency,amount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully processed bulk conversions",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ConversionResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid file format or content",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error during processing",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/bulk-convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ConversionResponseDTO>> bulkConvert(
            @RequestParam("file") MultipartFile file) {
        List<ConversionResponseDTO> responses = bulkConversionService.processBulkConversionFile(file);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
}
