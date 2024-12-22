package com.rundown.financeTracking.repository;

import com.rundown.financeTracking.entity.Transaction;
import com.rundown.financeTracking.entity.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {


    @Query("SELECT u from User u where u.username = :username")
    User findUserByName(String username);
}
