package com.yh.budgetly.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "savings", schema = "public")
public class Savings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long savingsId;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @Column(name = "month_year")
    private String monthYear;

    @Column(name = "total_expenses")
    private BigDecimal totalExpenses;

    @Column(name = "savings_amount")
    private BigDecimal savingsAmount;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "savings_goal")
    private BigDecimal savingsGoal;
}
