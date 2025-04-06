package com.yh.budgetly.repository;

import com.yh.budgetly.entity.Income;
import com.yh.budgetly.entity.User;
import com.yh.budgetly.rest.responses.dashboard.MonthlyIncome;
import com.yh.budgetly.rest.responses.dashboard.MonthlyTotal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findAllByUser(User user);

    Optional<Income> findBySourceName(String sourceName);


    @Query(value = "SELECT EXTRACT(MONTH FROM i.income_date)::VARCHAR AS monthNum, SUM(i.amount) AS amountSpent " +
            "FROM income i " +
            "WHERE i.user_id::VARCHAR = :userId AND EXTRACT(YEAR FROM i.income_date) = :year " +
            "GROUP BY monthNum ORDER BY monthNum", nativeQuery = true)
    List<MonthlyIncome> findAllMonthAndAllIncome(
            @Param("userId") String userId,
            @Param("year") int year);
}
