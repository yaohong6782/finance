package com.yh.budgetly.repository;

import com.yh.budgetly.entity.Income;
import com.yh.budgetly.entity.User;
import com.yh.budgetly.rest.responses.dashboard.MonthlyIncome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findAllByUser(User user);

    Optional<Income> findBySourceName(String sourceName);

    List<Income> findAllBySourceName(String sourceName);

    @Query(value = "SELECT EXTRACT(MONTH FROM i.income_date)::VARCHAR AS monthNum, SUM(i.amount) AS amountSpent " +
            "FROM income i " +
            "WHERE i.user_id::VARCHAR = :userId AND EXTRACT(YEAR FROM i.income_date) = :year " +
            "GROUP BY monthNum ORDER BY monthNum", nativeQuery = true)
    List<MonthlyIncome> findAllMonthAndAllIncome(
            @Param("userId") String userId,
            @Param("year") int year);

    @Query(value = "SELECT amount FROM Income i " +
            "WHERE i.user.id = :userId " +
            "AND sourceName = 'Corporate Job' " +
            "ORDER by createdAt desc")
    Optional<BigDecimal> findLatestIncome(
            @Param("userId") String userId
    );

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i " +
            "WHERE i.user.id = :userId " +
            "AND EXTRACT(MONTH FROM i.createdAt) = :month " +
            "AND EXTRACT(YEAR FROM i.createdAt) = :year")
    Optional<BigDecimal> findTotalIncomeThisMonth(@Param("userId") String userId,
                                        @Param("month") int month,
                                        @Param("year") int year);

    @Query("SELECT COUNT(DISTINCT i.sourceName) FROM Income i " +
            "WHERE i.user.id = :userId " +
            "AND MONTH(i.createdAt) = MONTH(CURRENT_DATE) " +
            "AND YEAR(i.createdAt) = YEAR(CURRENT_DATE)")
    Long countIncomeSourcesThisMonth(@Param("userId") String userId);

    @Query("SELECT COUNT(i) FROM Income i " +
            "WHERE i.user.id = :userId " +
            "AND i.sourceName = 'Corporate Job' " +
            "AND EXTRACT(MONTH FROM i.incomeDate) = :month " +
            "AND EXTRACT(YEAR FROM i.incomeDate) = :year")
    Long countCorporateJobForMonthAndYear(
            @Param("userId") String userId,
            @Param("month") int month,
            @Param("year") int year);
}
