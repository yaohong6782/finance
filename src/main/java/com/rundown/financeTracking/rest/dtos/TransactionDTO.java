package com.rundown.financeTracking.rest.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {
    private BigDecimal amount;
    private String description;
    private LocalDate transactionDate;
    private LocalDate createdAt;
}
