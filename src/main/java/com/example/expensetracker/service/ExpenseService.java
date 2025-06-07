package com.example.expensetracker.service;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.ExpenseRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.time.LocalDate;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public void saveExpense(Expense expense) {
        expenseRepository.save(expense);
    }

    public List<Expense> findByUser(User user) {
        return expenseRepository.findAllByUserOrderByDateDesc(user);
    }

    public void deleteAllByIds(List<Long> ids) {
        expenseRepository.deleteAllById(ids);
    }

    public Expense findById(Long id) {
        return expenseRepository.findById(id).orElse(null);
    }

    public List<Expense> findByUserAndDateRange(User user, LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            return expenseRepository.findByUserAndDateBetweenOrderByDateDesc(user, from, to);
        } else if (from != null) {
            return expenseRepository.findByUserAndDateAfterOrderByDateDesc(user, from.minusDays(1));
        } else if (to != null) {
            return expenseRepository.findByUserAndDateBeforeOrderByDateDesc(user, to.plusDays(1));
        } else {
            return expenseRepository.findAllByUserOrderByDateDesc(user);
        }
    }

    public Map<Integer, List<Integer>> getYearsWithMonthsWithExpenses() {
        List<Object[]> yearMonthPairs = expenseRepository.findDistinctYearAndMonth();

        Map<Integer, List<Integer>> map = new LinkedHashMap<>();

        for (Object[] pair : yearMonthPairs) {
            Integer year = (Integer) pair[0];
            Integer month = (Integer) pair[1];

            map.computeIfAbsent(year, k -> new ArrayList<>()).add(month);
        }

        return map;
    }

    public List<Expense> getExpensesByMonth(int year, int month) {
        return expenseRepository.findByYearAndMonth(year, month);
    }

    public List<Expense> getExpensesByMonthForUser(User user, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return expenseRepository.findByUserAndDateBetween(user, start, end);
    }

    public Map<Integer, BigDecimal> getYearlyTotals(User user) {
        List<Object[]> results = expenseRepository.sumAmountGroupedByYear(user.getId());

        Map<Integer, BigDecimal> yearlyTotals = new TreeMap<>(Collections.reverseOrder());
        for (Object[] row : results) {
            Integer year = (Integer) row[0];
            Object totalObj = row[1];
            BigDecimal total;

            if (totalObj instanceof Double) {
                total = BigDecimal.valueOf((Double) totalObj);
            } else if (totalObj instanceof BigDecimal) {
                total = (BigDecimal) totalObj;
            } else {
                total = BigDecimal.ZERO;
            }

            yearlyTotals.put(year, total);
        }

        return yearlyTotals;
    }

    public Map<Integer, BigDecimal> getMonthlyTotalsByYear(User user, int year) {
        // Здесь вызываем репозиторий для получения суммы по месяцам
        List<Object[]> results = expenseRepository.sumAmountGroupedByMonth(user.getId(), year);

        Map<Integer, BigDecimal> monthlyTotals = new TreeMap<>();

        for (Object[] row : results) {
            Integer month = (Integer) row[0];
            Object totalObj = row[1];
            BigDecimal total;

            if (totalObj instanceof Double) {
                total = BigDecimal.valueOf((Double) totalObj);
            } else if (totalObj instanceof BigDecimal) {
                total = (BigDecimal) totalObj;
            } else {
                total = BigDecimal.ZERO;
            }
            monthlyTotals.put(month, total);
        }

        return monthlyTotals;
    }
}
