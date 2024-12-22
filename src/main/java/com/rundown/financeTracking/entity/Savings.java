package com.rundown.financeTracking.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "savings", schema = "public")
public class Savings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long savingsId;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @Column(name = "month_year")
    private LocalDate monthYear;

    @Column(name = "total_income", nullable = false)
    private BigDecimal totalIncome;

    @Column(name = "total_expenses")
    private BigDecimal totalExpenses;

    @Column(name = "savings")
    private BigDecimal savings;

    @Column(name = "created_id")
    private LocalDate createdAt;
}
