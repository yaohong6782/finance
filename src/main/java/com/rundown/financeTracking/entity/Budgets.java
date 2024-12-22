package com.rundown.financeTracking.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "budgets", schema = "public")
public class Budgets {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long budgetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "month_year", nullable = false)
    private LocalDate monthYear;

    @Column(name = "total_budget", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalBudget;

    @Column(name = "savings_goal", nullable = false, precision = 10, scale = 2)
    private BigDecimal savingsGoal;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
}
