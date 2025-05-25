package mustceng.forex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mustceng.forex.exception.ApiIntegrationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class ExchangeRateApiComClient implements ExchangeRateApiClient {

    @Value("${forex.api.base-url}")
    private String baseUrl;

    @Value("${forex.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ExchangeRateApiComClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetches the exchange rate from ExchangeRate-API.com.
     * The API structure for a pair conversion is:
     * https://v6.exchangerate-api.com/v6/{apiKey}/pair/{base_code}/{target_code}
     *
     * @param baseCurrency The base currency code (e.g., "USD").
     * @param targetCurrency The target currency code (e.g., "EUR").
     * @return The exchange rate as a BigDecimal.
     * @throws ApiIntegrationException if there's an issue with the external API or invalid currency codes.
     */
    @Override
    @Cacheable(value = "exchangeRates", key = "#baseCurrency + '-' + #targetCurrency")
    public BigDecimal getExchangeRate(String baseCurrency, String targetCurrency) {
        String url = String.format("%s%s/pair/%s/%s", baseUrl, apiKey, baseCurrency, targetCurrency);
        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            String result = root.path("result").asText();
            if ("success".equals(result)) {
                BigDecimal rate = new BigDecimal(root.path("conversion_rate").asText());
                return rate;
            } else {
                String errorType = root.path("error-type").asText();
                throw new ApiIntegrationException("External API error: " + errorType + " for " + baseCurrency + " to " + targetCurrency);
            }
        } catch (HttpClientErrorException e) {
            // Handle HTTP client errors (e.g., 400 Bad Request, 404 Not Found, 403 Forbidden)
            String errorMessage = String.format("HTTP Error from external API for %s to %s: %s - %s",
                    baseCurrency, targetCurrency, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ApiIntegrationException(errorMessage, e);
        } catch (Exception e) {
            throw new ApiIntegrationException("Failed to fetch exchange rate from external API for " + baseCurrency + " to " + targetCurrency + ": " + e.getMessage(), e);
        }
    }
}
