package com.yh.budgetly.rest.responses.dashboard;

import com.yh.budgetly.rest.dtos.SavingsDTO;
import com.yh.budgetly.rest.dtos.TransactionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {
    private List<TransactionDTO> transactionDTO;
    private List<MonthlyTotal> monthlyTotals;
    private SavingsDTO savingsDTO;
    private BigDecimal currentMonthExpenses;
}
