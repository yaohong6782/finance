package com.yh.budgetly.rest.responses.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyFinanceDTO {
    private BigDecimal spent;
    private BigDecimal income;
    private BigDecimal creditCardPayment;
    private BigDecimal amountSaved;
//    private BigDecimal debitPayment;
}
