package com.yh.budgetly.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yh.budgetly.exceptions.CustomException;
import com.yh.budgetly.exceptions.ErrorResponse;
import com.yh.budgetly.rest.dtos.TransactionDTO;
import com.yh.budgetly.rest.requests.TransactionFields;
import com.yh.budgetly.rest.requests.TransactionRequestFields;
import com.yh.budgetly.rest.requests.TransactionSearchFields;
import com.yh.budgetly.rest.requests.TransactionSummaryFields;
import com.yh.budgetly.rest.responses.dashboard.DashboardResponse;
import com.yh.budgetly.service.TransactionService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/transactions")
public class TransactionsController {

    private final TransactionService transactionService;

    @Tag(name = "Transaction", description = "This API records the transactions user have entered")
    @PostMapping(value = "/addTransaction", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully",
                    content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = DashboardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied",
                    content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Unable to find requested endpoint",
                    content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TransactionDTO>> addTransaction(
            @RequestPart("user") String user,
            @RequestPart("transactions") String transactionsJson,
            @RequestParam(required = false) Map<String, MultipartFile> fileMap
    ) {
        log.info("add transaction initialised");

        ObjectMapper mapper = new ObjectMapper();
        List<TransactionFields> transactions;
        try {
            transactions = Arrays.asList(
                    mapper.readValue(transactionsJson, TransactionFields[].class)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Invalid transactions JSON", e);
        }

        TransactionRequestFields requestFields = new TransactionRequestFields();
        requestFields.setUser(user);

        // Attach files back to transactions based on index
        for (int i = 0; i < transactions.size(); i++) {
            TransactionFields tf = transactions.get(i);
            if (tf.getFileIndex() != null) {
                MultipartFile file = fileMap.get("file" + tf.getFileIndex());
                tf.setFile(file);
            }
        }

        requestFields.setTransactions(transactions);

        List<TransactionDTO> result = transactionService.addTransaction(requestFields, fileMap);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }


    @Tag(name = "Transaction", description = "This API records the transactions summaries for user")
    @PostMapping("/transactionSummary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Unable to find requested endpoint",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<TransactionDTO>> transactionSummary(@RequestBody TransactionSummaryFields transactionSummaryFields,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size) throws CustomException {
        String username = transactionSummaryFields.getUsername();
        log.info("Transaction summary for user : {} , page : {} , size : {} " , username, page, size);
        Page<TransactionDTO> transactionDTOList = transactionService.transactionPageSummary(username, page, size);

        return new ResponseEntity<>(transactionDTOList, HttpStatus.OK);
    }


    @Tag(name = "Transaction", description = "This API searches the transactions summaries for user")
    @PostMapping("/transactionSummarySearch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Unable to find requested endpoint",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<TransactionDTO>> searchTransactionSummary(@RequestBody TransactionSearchFields transactionSearchFields,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) throws CustomException {

        log.info("Transaction Summary Search triggered");
        log.info("Request to search : {} " , transactionSearchFields);
        Page<TransactionDTO> transactionDTOS = transactionService.searchTransactionPageSummary(transactionSearchFields, page, size);
        return new ResponseEntity<>(transactionDTOS, HttpStatus.OK);
    }
}
