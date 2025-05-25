package mustceng.forex.service;

import mustceng.forex.dto.ExchangeRateResponseDTO;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class ExchangeRateService {

    private final ExchangeRateApiClient exchangeRateApiClient;

    public ExchangeRateService(ExchangeRateApiClient exchangeRateApiClient) {
        this.exchangeRateApiClient = exchangeRateApiClient;
    }

    /**
     * Retrieves the current exchange rate between two currencies.
     *
     * @param sourceCurrency The source currency code (e.g., "USD").
     * @param targetCurrency The target currency code (e.g., "EUR").
     * @return An ExchangeRateResponse containing the rate.
     */
    public ExchangeRateResponseDTO getExchangeRate(String sourceCurrency, String targetCurrency) {
        BigDecimal rate = exchangeRateApiClient.getExchangeRate(sourceCurrency.toUpperCase(), targetCurrency.toUpperCase());
        return new ExchangeRateResponseDTO(sourceCurrency.toUpperCase(), targetCurrency.toUpperCase(), rate);
    }
}
