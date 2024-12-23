package com.rundown.financeTracking.mapper;

import com.rundown.financeTracking.entity.Transaction;
import com.rundown.financeTracking.rest.dtos.TransactionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "transactionDate", target = "transactionDate")
    @Mapping(source = "createdAt", target = "createdAt")
    TransactionDTO toTransactionDTO(Transaction transaction);

    // Map a list of transactions to a list of TransactionDTOs
    List<TransactionDTO> toTransactionDTOList(List<Transaction> transactions);
}
