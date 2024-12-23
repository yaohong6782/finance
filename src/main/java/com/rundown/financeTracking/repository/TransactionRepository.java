package com.rundown.financeTracking.repository;

import com.rundown.financeTracking.entity.Transaction;
import com.rundown.financeTracking.entity.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

//    @Query("SELECT t FROM Transaction t JOIN FETCH t.categories WHERE t.user.username = :username")
//    List<Transaction> findTransactionsWithCategoriesByUsername(@Param("username") String username);

    @Query("SELECT t from Transaction t where t.user.userId = :userId")
    List<Transaction> findUserTransactionById(String userId);

    @Query("SELECT u from User u where u.username = :username")
    User findUserByName(String username);

}
