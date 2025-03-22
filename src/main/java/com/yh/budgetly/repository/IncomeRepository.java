package com.yh.budgetly.repository;

import com.yh.budgetly.entity.Income;
import com.yh.budgetly.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findAllByUser(User user);

    Optional<Income> findBySourceName(String sourceName);
}
