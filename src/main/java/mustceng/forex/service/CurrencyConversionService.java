package mustceng.forex.service;

import mustceng.forex.dto.ConversionRequestDTO;
import mustceng.forex.dto.ConversionResponseDTO;
import mustceng.forex.model.ConversionTransaction;
import mustceng.forex.repository.ConversionTransactionRepository;
import mustceng.forex.util.TransactionIdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class CurrencyConversionService {

    private final ExchangeRateService exchangeRateService;
    private final ConversionTransactionRepository transactionRepository;

    public CurrencyConversionService(ExchangeRateService exchangeRateService, ConversionTransactionRepository transactionRepository) {
        this.exchangeRateService = exchangeRateService;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Performs a currency conversion and records the transaction.
     *
     * @param request The conversion request containing source currency, target currency, and amount.
     * @return A ConversionResponse with the converted amount and transaction details.
     */
    @Transactional
    public ConversionResponseDTO convertCurrency(ConversionRequestDTO request) {
        String sourceCurrency = request.getSourceCurrency().toUpperCase();
        String targetCurrency = request.getTargetCurrency().toUpperCase();
        BigDecimal amount = request.getAmount();

        // Get the exchange rate
        BigDecimal exchangeRate = exchangeRateService.getExchangeRate(sourceCurrency, targetCurrency).getRate();

        // Perform the conversion
        BigDecimal convertedAmount = amount.multiply(exchangeRate).setScale(4, RoundingMode.HALF_UP);

        // Generate a unique transaction ID
        String transactionId = TransactionIdGenerator.generateUniqueTransactionId();

        // Save the transaction to the database
        ConversionTransaction transaction = new ConversionTransaction();
        transaction.setTransactionId(transactionId);
        transaction.setSourceCurrency(sourceCurrency);
        transaction.setTargetCurrency(targetCurrency);
        transaction.setOriginalAmount(amount);
        transaction.setConvertedAmount(convertedAmount);
        transaction.setExchangeRate(exchangeRate);
        transaction.setTransactionDate(LocalDateTime.now());

        transactionRepository.save(transaction);

        return new ConversionResponseDTO(
                transactionId,
                sourceCurrency,
                targetCurrency,
                amount,
                convertedAmount,
                exchangeRate
        );
    }
}
