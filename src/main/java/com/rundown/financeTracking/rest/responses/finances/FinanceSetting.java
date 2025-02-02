package com.rundown.financeTracking.rest.responses.finances;

import com.rundown.financeTracking.rest.dtos.IncomeDTO;
import com.rundown.financeTracking.rest.dtos.SavingsDTO;
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
