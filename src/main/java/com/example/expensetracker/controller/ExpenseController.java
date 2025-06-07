package com.example.expensetracker.controller;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.User;
import com.example.expensetracker.service.ExpenseService;
import com.example.expensetracker.service.ExchangeRateService;
import com.example.expensetracker.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserService userService;
    private final ExchangeRateService exchangeRateService;

    public ExpenseController(ExpenseService expenseService,
                             UserService userService,
                             ExchangeRateService exchangeRateService) {

        this.expenseService = expenseService;
        this.userService = userService;
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping("/list")
    public String showExpenseList(@RequestParam(required = false) Integer year,
                               @RequestParam(required = false) Integer month,
                               @RequestParam(required = false) String currency,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                               Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        if (user == null) {
            return "redirect:/login";
        }

        List<Expense> expenses;

        // Фильтрация по году и месяцу
        if (year != null && month != null) {
            expenses = expenseService.getExpensesByMonthForUser(user, year, month);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
        } else {
            // Фильтрация по диапазону дат
            expenses = expenseService.findByUserAndDateRange(user, dateFrom, dateTo);
        }

        model.addAttribute("expenses", expenses);
        model.addAttribute("selectedCurrency", currency);

        // Конвертация валют
        Map<Long, String> convertedMap = new HashMap<>();
        if (currency != null && !currency.isBlank()) {
            for (Expense expense : expenses) {
                BigDecimal rate = exchangeRateService.getExchangeRate(
                        expense.getCurrency(), currency, expense.getDate());

                if (rate == null) {
                    convertedMap.put(expense.getId(), "Не удалось найти курс валюты на эту дату");
                    continue;
                }

                BigDecimal amount = BigDecimal.valueOf(expense.getAmount());
                BigDecimal converted = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
                convertedMap.put(expense.getId(), converted.toString());
            }
        } else {
            convertedMap = Collections.emptyMap();
        }

        // Доп. данные для сайдбара и фильтров
        model.addAttribute("convertedMap", convertedMap);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("currencies", exchangeRateService.getAvailableCurrencies());
        model.addAttribute("yearsWithMonths", expenseService.getYearsWithMonthsWithExpenses());

        return "expense-list";
    }

    @GetMapping("/summary")
    public String showAllYearsSummary(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.findByUsername(username);

        if (user == null) {
            return "redirect:/login";
        }

        Map<Integer, BigDecimal> yearlyTotals = expenseService.getYearlyTotals(user);
        Map<Integer, List<Integer>> yearsWithMonths = expenseService.getYearsWithMonthsWithExpenses();

        model.addAttribute("yearlyTotals", yearlyTotals);
        model.addAttribute("yearsWithMonths", yearsWithMonths);

        return "expense-summary";
    }

    @GetMapping("/summary/year/{year}")
    public String showYearSummary(
            @PathVariable int year,
            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.findByUsername(username);

        // Получаем сводку по месяцам конкретного года для пользователя
        Map<Integer, BigDecimal> monthlyTotals = expenseService.getMonthlyTotalsByYear(user, year);

        model.addAttribute("year", year);
        model.addAttribute("monthlyTotals", monthlyTotals);
        model.addAttribute("yearsWithMonths", expenseService.getYearsWithMonthsWithExpenses());

        return "expense-yearly";
    }

    // Показ формы
    @GetMapping("/add")
    public String showAddExpenseForm(Model model, HttpServletRequest request) {
        String referer = request.getHeader("referer");
        model.addAttribute("returnUrl", referer);
        return "add-expense";
    }

    // Обработка формы
    @PostMapping("/add")
    public String addExpense(@RequestParam String title,
                             @RequestParam Double amount,
                             @RequestParam String currency,
                             @RequestParam String date,
                             @RequestParam(required = false) String returnUrl,
                             RedirectAttributes redirectAttributes) {

        Expense expense = new Expense();
        expense.setTitle(title);
        expense.setAmount(amount);
        expense.setCurrency(currency);
        expense.setDate(LocalDate.parse(date));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        expense.setUser(user);
        expenseService.saveExpense(expense);

        redirectAttributes.addFlashAttribute("message", "Расход успешно добавлен!");

        return "redirect:" + (returnUrl != null && !returnUrl.isBlank() ? returnUrl : "/expenses/list");
    }

    @PostMapping("/deleteSelected")
    public String deleteSelectedExpenses(@RequestParam(name = "selectedIds", required = false) List<Long> selectedIds,
                                         RedirectAttributes redirectAttributes) {
        if (selectedIds != null && !selectedIds.isEmpty()) {
            expenseService.deleteAllByIds(selectedIds);
            redirectAttributes.addFlashAttribute("message", "Выбранные расходы успешно удалены.");
        } else {
            redirectAttributes.addFlashAttribute("message", "Не выбраны расходы для удаления.");
        }
        return "redirect:/expenses/list";
    }

    @PostMapping("/updateAll")
    public String updateAllExpenses(@RequestParam Map<String, String> params,
                                    RedirectAttributes redirectAttributes) {

        for (String key : params.keySet()) {
            if (key.startsWith("id_")) {
                Long id = Long.parseLong(key.substring(3));

                Expense expense = expenseService.findById(id);
                if (expense != null) {
                    expense.setTitle(params.get("title_" + id));
                    expense.setAmount(Double.parseDouble(params.get("amount_" + id)));
                    expense.setCurrency(params.get("currency_" + id));
                    expense.setDate(LocalDate.parse(params.get("date_" + id)));
                    expenseService.saveExpense(expense);
                }
            }
        }

        redirectAttributes.addFlashAttribute("message", "Изменения сохранены.");
        return "redirect:/expenses/list";
    }
}
