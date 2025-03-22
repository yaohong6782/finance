package com.yh.budgetly.rest.responses.transactions;

import com.yh.budgetly.rest.dtos.TransactionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionAdded {
    private List<TransactionDTO> transactionDTO;
}
