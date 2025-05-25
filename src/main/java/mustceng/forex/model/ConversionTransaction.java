package mustceng.forex.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversion_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversionTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String transactionId; // Unique transaction identifier

    @Column(nullable = false)
    private String sourceCurrency;

    @Column(nullable = false)
    private String targetCurrency;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal originalAmount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal convertedAmount;

    @Column(nullable = false, precision = 19, scale = 6) // Store rate with higher precision
    private BigDecimal exchangeRate;

    @Column(nullable = false)
    private LocalDateTime transactionDate;
}

