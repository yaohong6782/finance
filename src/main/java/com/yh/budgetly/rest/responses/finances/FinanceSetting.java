package com.yh.budgetly.rest.responses.finances;

import com.yh.budgetly.rest.dtos.IncomeDTO;
import com.yh.budgetly.rest.dtos.SavingsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinanceSetting {
    private List<IncomeDTO> incomeDTO;
    private List<SavingsDTO> savingsDTO;
    private String totalSumCurrentMonth;
}
