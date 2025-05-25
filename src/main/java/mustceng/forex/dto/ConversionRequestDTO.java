package mustceng.forex.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversionRequestDTO {
    @NotBlank(message = "Source currency cannot be empty")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Source currency must be a 3-letter uppercase code (e.g., USD)")
    private String sourceCurrency;

    @NotBlank(message = "Target currency cannot be empty")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Target currency must be a 3-letter uppercase code (e.g., EUR)")
    private String targetCurrency;

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;
}
