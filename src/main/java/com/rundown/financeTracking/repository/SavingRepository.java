package com.rundown.financeTracking.repository;

import com.rundown.financeTracking.entity.Savings;
import com.rundown.financeTracking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SavingRepository extends JpaRepository<Savings, Long> {
    List<Savings> findAllByUser(User user);

    @Query("SELECT COALESCE(SUM(s.savingsAmount), 0) FROM Savings s WHERE s.user = :user")
    Long findUserTotalSavings(@Param(("user")) User user);


    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.user = :user AND YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month")
    Long findCurrentMonthTotalExpenses(@Param("user") User user, @Param("year") int year, @Param("month") int month);

    @Query("SELECT s FROM Savings s WHERE s.user.id = :userId AND s.monthYear = :monthYear")
    Optional<Savings> findByUserIdAndMonthYear(@Param("userId") Long userId, @Param("monthYear") String monthYear);


    @Modifying
    @Query("UPDATE Savings s SET s.totalExpenses = :totalExpenses, s.savingsGoal = :savingsGoal, s.createdAt = :createdAt WHERE s.user.id = :userId AND s.monthYear = :monthYear")
    int updateSavings(@Param("totalExpenses") String totalExpenses,
                      @Param("savingsGoal") String savingsGoal,
                      @Param("createdAt") LocalDate createdAt,
                      @Param("userId") Long userId,
                      @Param("monthYear") String monthYear);

}
