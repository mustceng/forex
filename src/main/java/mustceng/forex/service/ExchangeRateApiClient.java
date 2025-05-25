package mustceng.forex.service;

import java.math.BigDecimal;

/**
 * Interface for integrating with external exchange rate API providers.
 */
public interface ExchangeRateApiClient {

    /**
     * Fetches the exchange rate between a source and target currency.
     *
     * @param baseCurrency The base currency code (e.g., "USD").
     * @param targetCurrency The target currency code (e.g., "EUR").
     * @return The exchange rate as a BigDecimal.
     * @throws com.example.forex.exception.ApiIntegrationException if there's an issue with the external API.
     */
    BigDecimal getExchangeRate(String baseCurrency, String targetCurrency);
}