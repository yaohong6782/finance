package com.rundown.financeTracking.rest.responses.transactions;

import com.rundown.financeTracking.rest.dtos.TransactionDTO;
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
