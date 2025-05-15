package com.yh.budgetly.rest.requests;

import com.yh.budgetly.entity.Files;
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
    // Add new transactions
    private BigDecimal amount;
    private String category;
    private String transactionDate;
    private String description;
    private String paymentMethod;

    private Files files;
}
