package com.yh.budgetly.rest.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpenseTrendDTO {
    private BigDecimal currentMonthExpense;
    private BigDecimal previousMonthExpense;
    private BigDecimal incomeNetPercentage;
    private BigDecimal expensesNetPercentage;
    private BigDecimal savingsNetPercentage;
}
