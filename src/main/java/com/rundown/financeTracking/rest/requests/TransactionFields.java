package com.rundown.financeTracking.rest.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionFields {
    private String username;
    private BigDecimal amount;
    private String category;
    private String transactionDate;
    private String description;
}
