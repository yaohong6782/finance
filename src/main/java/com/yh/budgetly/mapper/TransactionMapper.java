package com.yh.budgetly.mapper;

import com.yh.budgetly.entity.Transaction;
import com.yh.budgetly.rest.dtos.TransactionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "transactionDate", target = "transactionDate")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "categories", target = "categoriesDTO")
    TransactionDTO toTransactionDTO(Transaction transaction);


    List<TransactionDTO> toTransactionDTOList(List<Transaction> transactions);

//    Page<TransactionDTO> toTransactionDTOList(Page<Transaction> transactions);

    default Page<TransactionDTO> toTransactionDTOPage(Page<Transaction> transactionPage) {
        return transactionPage.map(this::toTransactionDTO);
    }
}
