package com.example.expensetracker;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.service.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
    }

    @Test
    void testFindByUserAndDateRange() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        Expense e1 = new Expense();
        e1.setTitle("Test 1");
        e1.setDate(LocalDate.of(2024, 5, 10));
        e1.setAmount(100.0);
        e1.setUser(testUser);

        Expense e2 = new Expense();
        e2.setTitle("Test 2");
        e2.setDate(LocalDate.of(2024, 6, 5));
        e2.setAmount(200.0);
        e2.setUser(testUser);

        List<Expense> mockResult = Arrays.asList(e1, e2);
        when(expenseRepository.findByUserAndDateBetweenOrderByDateDesc(eq(testUser), eq(from), eq(to)))
                .thenReturn(mockResult);

        List<Expense> result = expenseService.findByUserAndDateRange(testUser, from, to);

        assertEquals(2, result.size());
        assertEquals("Test 1", result.get(0).getTitle());
        verify(expenseRepository, times(1)).findByUserAndDateBetweenOrderByDateDesc(eq(testUser), eq(from), eq(to));
    }

    @Test
    void testFindByUserWithoutDateRange() {
        when(expenseRepository.findAllByUserOrderByDateDesc(testUser)).thenReturn(List.of());

        List<Expense> result = expenseService.findByUserAndDateRange(testUser, null, null);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(expenseRepository, times(1)).findAllByUserOrderByDateDesc(testUser);
    }
}