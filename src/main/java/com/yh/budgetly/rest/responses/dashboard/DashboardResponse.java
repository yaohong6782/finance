package com.yh.budgetly.rest.responses.dashboard;

import com.yh.budgetly.rest.dtos.SavingsDTO;
import com.yh.budgetly.rest.dtos.TransactionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {
    private List<TransactionDTO> transactionDTO;
    private Map<String, BigDecimal> financeBreakDown;
    private Map<Integer, MonthlyFinanceDTO> monthlyFinanceDTO;
    private Map<Integer, BigDecimal> monthlyAmountSpent;
    private Map<Integer, BigDecimal> monthlyIncome;
    private SavingsDTO savingsDTO;
    private BigDecimal currentMonthExpenses;
}
