package mustceng.forex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateResponseDTO {
    private String sourceCurrency;
    private String targetCurrency;
    private BigDecimal rate;
}