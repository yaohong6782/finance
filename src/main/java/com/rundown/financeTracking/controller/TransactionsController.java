package com.rundown.financeTracking.controller;

import com.rundown.financeTracking.exceptions.CustomException;
import com.rundown.financeTracking.repository.TransactionRepository;
import com.rundown.financeTracking.rest.dtos.IncomeDTO;
import com.rundown.financeTracking.rest.dtos.TransactionDTO;
import com.rundown.financeTracking.rest.requests.IncomeRequest;
import com.rundown.financeTracking.rest.requests.TransactionFields;
import com.rundown.financeTracking.rest.requests.TransactionRequestFields;
import com.rundown.financeTracking.rest.requests.TransactionSummaryFields;
import com.rundown.financeTracking.service.TransactionService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/transactions")
public class TransactionsController {

    private final TransactionService transactionService;

    @Tag(name = "Add transactions", description = "This API records the transactions user have entered")
    @PostMapping("/addTransaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", description = "Unable to find requested endpoint"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<List<TransactionDTO>> addTransaction(@RequestBody TransactionRequestFields transactionRequestFields) throws CustomException {
        log.info("Add transaction request : {} ", transactionRequestFields.getTransactionFields().toString());

        List<TransactionDTO> transactionDTO = transactionService.addTransaction(transactionRequestFields);

        return new ResponseEntity<>(transactionDTO, HttpStatus.CREATED);
    }


    @Tag(name = "Transaction Summary", description = "This API records the transactions summaries for user")
    @PostMapping("/transactionSummary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", description = "Unable to find requested endpoint"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<?> transactionSummary(@RequestBody TransactionSummaryFields transactionSummaryFields) throws CustomException {
        String username = transactionSummaryFields.getUsername();
        log.info("Transaction summary for user : {} " , username);
        transactionService.transactionSummary(username);
//
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
