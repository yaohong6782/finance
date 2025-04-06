package com.yh.budgetly.repository;

import com.yh.budgetly.entity.Transaction;
import com.yh.budgetly.rest.responses.dashboard.MonthlyTotal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t from Transaction t where t.user.userId = :userId")
    List<Transaction> findUserTransactionByIdWhereClause(String userId, Pageable pageable);

    // Utilised only when data set is small otherwise use Page - transactionPageSummary function
    @Query("SELECT t from Transaction t JOIN t.user u where u.userId = :userId")
    List<Transaction> findUserTransactionById(String userId );

    @Query("SELECT t from Transaction t " +
            "LEFT JOIN FETCH t.file f " +
            "JOIN t.user u where u.userId = :userId")
    Page<Transaction> findUserTransactionByIdPagination(String userId, Pageable pageable);

    @Query("SELECT t FROM Transaction t " +
            "LEFT JOIN FETCH t.file f " +  // Fetch files if available
            "WHERE t.user.userId = :userId " +
            "AND (:category IS NULL OR (t.categories.type IS NOT NULL AND LOWER(t.categories.type) LIKE CONCAT('%', LOWER(:category), '%'))) " +
//            "AND (:category IS NULL OR (t.categories.type IS NOT NULL AND LOWER(t.categories.type) = LOWER(:category))) " +
            "AND (:amountMax IS NULL OR t.amount <= :amountMax)")
    Page<Transaction> findUserTransactionSearchesByIdPagination(
            String userId,
            @Param("category") String category,
            @Param("amountMax") Double amountMax,
            Pageable pageable
    );

//    @Query("SELECT t from Transaction t " +
//            "WHERE t.user.userId = :userId " +
//            "AND t.transactionDate >= FUNCTION('date_trunc', 'month', CURRENT_DATE) " +
//            "AND t.transactionDate < FUNCTION('date_trunc', 'month', CURRENT_DATE ) + INTERVAL 'MONTH'")
//    List<Transaction> findCurrentMonthTransactions(String userId);

    @Query("SELECT t from Transaction t " +
            "WHERE t.user.userId = :userId " +
            "AND t.transactionDate >= :startDate " +
            "AND t.transactionDate < :endDate")
    List<Transaction> findCurrentMonthTransactions(
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT EXTRACT(MONTH FROM t.transaction_date)::VARCHAR AS monthNum, SUM(t.amount) AS amountSpent " +
            "FROM transactions t " +
            "WHERE t.user_id::VARCHAR = :userId AND EXTRACT(YEAR FROM t.transaction_date) = :year " +
            "GROUP BY monthNum ORDER BY monthNum", nativeQuery = true)
    List<MonthlyTotal> findAllMonthAndTotalSpent(
            @Param("userId") String userId,
            @Param("year") int year);

}
