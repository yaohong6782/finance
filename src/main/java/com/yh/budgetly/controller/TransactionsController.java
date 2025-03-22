package com.yh.budgetly.controller;

import com.yh.budgetly.exceptions.CustomException;
import com.yh.budgetly.rest.dtos.TransactionDTO;
import com.yh.budgetly.rest.requests.TransactionRequestFields;
import com.yh.budgetly.rest.requests.TransactionSearchFields;
import com.yh.budgetly.rest.requests.TransactionSummaryFields;
import com.yh.budgetly.service.TransactionService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/transactions")
public class TransactionsController {

    private final TransactionService transactionService;

    @Tag(name = "Transaction", description = "This API records the transactions user have entered")
    @PostMapping("/addTransaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", description = "Unable to find requested endpoint"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<List<TransactionDTO>> addTransaction(@RequestBody TransactionRequestFields transactionRequestFields) throws CustomException {
        log.info("Add transaction request : {} ", transactionRequestFields.getTransactions());

        List<TransactionDTO> transactionDTO = transactionService.addTransaction(transactionRequestFields);

        return new ResponseEntity<>(transactionDTO, HttpStatus.CREATED);
    }


    @Tag(name = "Transaction", description = "This API records the transactions summaries for user")
    @PostMapping("/transactionSummary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", description = "Unable to find requested endpoint"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<Page<TransactionDTO>> transactionSummary(@RequestBody TransactionSummaryFields transactionSummaryFields,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "2") int size) throws CustomException {
        String username = transactionSummaryFields.getUsername();
        log.info("Transaction summary for user : {} , page : {} , size : {} " , username, page, size);
        Page<TransactionDTO> transactionDTOList = transactionService.transactionPageSummary(username, page, size);

        return new ResponseEntity<>(transactionDTOList, HttpStatus.OK);
    }


    @Tag(name = "Transaction", description = "This API searches the transactions summaries for user")
    @PostMapping("/transactionSummarySearch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", description = "Unable to find requested endpoint"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<Page<TransactionDTO>> searchTransactionSummary(@RequestBody TransactionSearchFields transactionSearchFields,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "2") int size) throws CustomException {

        Page<TransactionDTO> transactionDTOS = transactionService.searchTransactionPageSummary(transactionSearchFields, page, size);
        return new ResponseEntity<>(transactionDTOS, HttpStatus.OK);
    }
}
