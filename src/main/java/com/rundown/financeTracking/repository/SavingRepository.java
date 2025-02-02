package com.rundown.financeTracking.repository;

import com.rundown.financeTracking.entity.Income;
import com.rundown.financeTracking.entity.Savings;
import com.rundown.financeTracking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingRepository extends JpaRepository<Savings, Long> {
    List<Savings> findAllByUser(User user);
}
