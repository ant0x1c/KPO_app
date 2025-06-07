package com.example.expensetracker.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ExchangeRateService {

    private final RestTemplate restTemplate = new RestTemplate();

    public BigDecimal getExchangeRate(String base, String target, LocalDate date) {
        if (base.equals(target)) {
            return BigDecimal.ONE;
        }

        String formattedDate = date.format(DateTimeFormatter.ISO_DATE); // формат yyyy-MM-dd
        String url = "https://api.frankfurter.app/" + formattedDate +
                "?from=" + base + "&to=" + target;

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("rates")) {
                return null;
            }

            Map<String, Object> rates = (Map<String, Object>) response.get("rates");
            Object rateObj = rates.get(target);

            if (rateObj instanceof Number) {
                return new BigDecimal(rateObj.toString());
            }

        } catch (Exception e) {
            //e.printStackTrace();
        }

        return null;
    }

    public Map<String, String> getAvailableCurrencies() {
        String url = "https://api.frankfurter.app/currencies";
        try {
            Map<String, String> currencies = restTemplate.getForObject(url, Map.class);
            if (currencies == null) {
                currencies = new HashMap<>();
            }

            // Добваляем рубли вручную, т.к. на данный момент API не поддерживает курс рубля
            currencies.putIfAbsent("RUB", "Russian Ruble");

            return currencies.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(
                            LinkedHashMap::new,
                            (m, e) -> m.put(e.getKey(), e.getValue()),
                            LinkedHashMap::putAll
                    );
        } catch (Exception e) {
            e.printStackTrace();

            // fallback — только RUB, если API недоступен
            return Map.of("RUB", "Russian Ruble");
        }
    }
}
