package com.rundown.financeTracking.repository;

import com.rundown.financeTracking.entity.Income;
import com.rundown.financeTracking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findAllByUser(User user);

    Optional<Income> findBySourceName(String sourceName);
}
