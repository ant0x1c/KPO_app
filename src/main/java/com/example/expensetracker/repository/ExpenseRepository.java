package com.example.expensetracker.repository;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDate;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findAllByUserOrderByDateDesc(User user);

    List<Expense> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate from, LocalDate to);

    List<Expense> findByUserAndDateAfterOrderByDateDesc(User user, LocalDate from);

    List<Expense> findByUserAndDateBeforeOrderByDateDesc(User user, LocalDate to);

    @Query("SELECT DISTINCT YEAR(e.date), MONTH(e.date) FROM Expense e ORDER BY YEAR(e.date) DESC, MONTH(e.date) ASC")
    List<Object[]> findDistinctYearAndMonth();

    @Query("SELECT e FROM Expense e WHERE YEAR(e.date) = :year AND MONTH(e.date) = :month ORDER BY e.date DESC")
    List<Expense> findByYearAndMonth(@Param("year") int year, @Param("month") int month);

    List<Expense> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);

    @Query("SELECT YEAR(e.date), SUM(e.amount) FROM Expense e WHERE e.user.id = :userId GROUP BY YEAR(e.date)")
    List<Object[]> sumAmountGroupedByYear(@Param("userId") Long userId);

    @Query("SELECT MONTH(e.date) as month, SUM(e.amount) " +
            "FROM Expense e WHERE e.user.id = :userId AND YEAR(e.date) = :year " +
            "GROUP BY MONTH(e.date) ORDER BY MONTH(e.date)")
    List<Object[]> sumAmountGroupedByMonth(@Param("userId") Long userId, @Param("year") int year);
}
