package com.yh.budgetly.service;

import com.yh.budgetly.constants.CommonVariables;
import com.yh.budgetly.entity.Categories;
import com.yh.budgetly.entity.Files;
import com.yh.budgetly.entity.Transaction;
import com.yh.budgetly.entity.User;
import com.yh.budgetly.exceptions.CustomException;
import com.yh.budgetly.mapper.TransactionMapper;
import com.yh.budgetly.repository.CategoryRepository;
import com.yh.budgetly.repository.FileRepository;
import com.yh.budgetly.repository.TransactionRepository;
import com.yh.budgetly.repository.UserRepository;
import com.yh.budgetly.rest.dtos.TransactionDTO;
import com.yh.budgetly.rest.requests.TransactionFields;
import com.yh.budgetly.rest.requests.TransactionRequestFields;
import com.yh.budgetly.rest.requests.TransactionSearchFields;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Builder
@Service
@AllArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final TransactionMapper transactionMapper;

    public List<TransactionDTO> addTransaction(TransactionRequestFields transactionRequestFields) {
        log.info("Processing transactions to be added : {} " , transactionRequestFields);

        String username = transactionRequestFields.getUser();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new CustomException(CommonVariables.USER_NOT_FOUND, HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase()));

        List<Transaction> transactionsList = new ArrayList<>();
        List<Categories> categoriesList = new ArrayList<>();


        for (TransactionFields transField : transactionRequestFields.getTransactions()) {
            Categories categories = categoryRepository.findByType(transField.getCategory().toLowerCase())
                    .orElseGet(() -> {
                Categories newCategory = new Categories();
                newCategory.setType(transField.getCategory());

                categoriesList.add(newCategory);
                return newCategory;
            });

            if (!categoriesList.isEmpty()) {
                categoryRepository.saveAll(categoriesList);
            }


            Transaction transaction = Transaction.builder()
                    .user(user)
                    .categories(categories)
                    .amount(transField.getAmount())
                    .transactionDate(LocalDate.parse(transField.getTransactionDate()))
                    .paymentMethod(transField.getPaymentMethod())
                    .description(transField.getDescription())
                    .build();


            Files file = transField.getFiles();
            if (file != null) {
                file.setTransaction(transaction);
                file.setUploadedAt(LocalDate.now());
                log.info("file info: {}", file);
//                fileRepository.save(file);
                transaction.setFile(file);
            }
            transactionsList.add(transaction);
            transactionRepository.saveAll(transactionsList);
        }

        List<TransactionDTO> transactionDTOS = transactionMapper.toTransactionDTOList(transactionsList);

        return transactionDTOS;
    }


    public List<TransactionDTO> transactionSummary(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new CustomException(CommonVariables.USER_NOT_FOUND, HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase()));

        String userId = String.valueOf(user.getUserId());

        PageRequest pageRequest = PageRequest.of(0,10);
        List<Transaction> transactionList = transactionRepository.findUserTransactionById(userId);
        log.info("transaction list : {} " , transactionList);

        List<TransactionDTO> transactionDTOList = transactionMapper.toTransactionDTOList(transactionList);

        return transactionDTOList;
    }

    public Page<TransactionDTO> transactionPageSummary(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new CustomException(CommonVariables.USER_NOT_FOUND, HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase()));

        String userId = String.valueOf(user.getUserId());
        PageRequest pageRequest = PageRequest.of(page,size);
        Page<Transaction> transactionPage = transactionRepository.findUserTransactionByIdPagination(userId, pageRequest);
        log.info("transaction page : {} ", transactionPage);

        return transactionMapper.toTransactionDTOPage(transactionPage);
    }

    public Page<TransactionDTO> searchTransactionPageSummary(TransactionSearchFields transactionSearchFields, int page, int size) {
        log.info("username : {} " , transactionSearchFields.getUsername());

        String category = transactionSearchFields.getSearchFields().getCategory().isBlank() ?
                "" : transactionSearchFields.getSearchFields().getCategory();

        String amount = transactionSearchFields.getSearchFields().getAmount().isBlank() ?
                null : transactionSearchFields.getSearchFields().getAmount();

        User user = userRepository.findByUsername(transactionSearchFields.getUsername())
                .orElseThrow(() ->
                        new CustomException(CommonVariables.USER_NOT_FOUND, HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase()));

        String userId = String.valueOf(user.getUserId());

        PageRequest pageRequest = PageRequest.of(page,size);
        Page<Transaction> transactionPage = transactionRepository.findUserTransactionSearchesByIdPagination(
                userId,
                category,
                amount != null ? Double.valueOf(amount) : null,
                pageRequest
        );
        log.info("transaction searched pages : {} ", transactionPage);

        return transactionMapper.toTransactionDTOPage(transactionPage);
    }
}
