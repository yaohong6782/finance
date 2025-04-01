package com.yh.budgetly.rest.requests;

import com.yh.budgetly.entity.Files;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequestFields {
    private String user;
    private List<TransactionFields> transactions;
}
