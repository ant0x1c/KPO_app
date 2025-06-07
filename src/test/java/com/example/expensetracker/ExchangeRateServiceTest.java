package com.example.expensetracker;

import com.example.expensetracker.service.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ExchangeRateServiceTest {

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void returnsOneWhenSameCurrency() {
        BigDecimal rate = exchangeRateService.getExchangeRate("USD", "USD", LocalDate.now());
        assertEquals(BigDecimal.ONE, rate);
    }

    @Test
    void returnsNullOnFailureOrInvalidDate() {
        // Заведомо неправильная дата (до 1999 года) => frankfurter.app не даст ответ
        BigDecimal rate = exchangeRateService.getExchangeRate("USD", "EUR", LocalDate.of(1990, 1, 1));
        assertNull(rate);
    }
}