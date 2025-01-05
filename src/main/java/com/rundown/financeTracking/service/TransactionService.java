package com.rundown.financeTracking.service;

import com.rundown.financeTracking.entity.Categories;
import com.rundown.financeTracking.entity.Transaction;
import com.rundown.financeTracking.entity.User;
import com.rundown.financeTracking.exceptions.CustomException;
import com.rundown.financeTracking.mapper.TransactionMapper;
import com.rundown.financeTracking.repository.CategoryRepository;
import com.rundown.financeTracking.repository.TransactionRepository;
import com.rundown.financeTracking.repository.UserRepository;
import com.rundown.financeTracking.rest.dtos.TransactionDTO;
import com.rundown.financeTracking.rest.requests.TransactionFields;
import com.rundown.financeTracking.rest.requests.TransactionRequestFields;
import com.rundown.financeTracking.rest.requests.TransactionSearchFields;
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
import java.util.Optional;

@Slf4j
@Builder
@Service
@AllArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private TransactionMapper transactionMapper;

    public List<TransactionDTO> addTransaction(TransactionRequestFields transactionRequestFields) {
        log.info("Processing transactions to be added : {} " , transactionRequestFields);

        String username = transactionRequestFields.getUser();
        User user = transactionRepository.findUserByName(username);
        log.info("User found : {} " , user);

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

            Transaction transaction = Transaction.builder()
                    .user(user)
                    .categories(categories)
                    .amount(transField.getAmount())
                    .transactionDate(LocalDate.parse(transField.getTransactionDate()))
                    .description(transField.getDescription())
                    .build();
            transactionsList.add(transaction);
        }

        if (!categoriesList.isEmpty()) {
           categoryRepository.saveAll(categoriesList);
        }

        transactionRepository.saveAll(transactionsList);

        List<TransactionDTO> transactionDTOS = transactionMapper.toTransactionDTOList(transactionsList);

        return transactionDTOS;
    }

    public List<TransactionDTO> transactionSummary(String username) {
        Optional<User> user = Optional.ofNullable(userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new CustomException("User does not exist", HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase())));

        String userId = "";
        if (user.isPresent()) {
            userId = String.valueOf(user.get().getUserId());
        }
        PageRequest pageRequest = PageRequest.of(0,10);
        List<Transaction> transactionList = transactionRepository.findUserTransactionById(userId, pageRequest);
        log.info("transaction list : {} " , transactionList);

        List<TransactionDTO> transactionDTOList = transactionMapper.toTransactionDTOList(transactionList);

        return transactionDTOList;
    }

    public Page<TransactionDTO> transactionPageSummary(String username, int page, int size) {
        Optional<User> user = Optional.ofNullable(userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new CustomException("User does not exist", HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase())));

        String userId = user.map(u -> String.valueOf(u.getUserId()))
                .orElseThrow(() -> new CustomException("Invalid user", HttpStatus.BAD_REQUEST, "Invalid user"));

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

        Optional<User> user = Optional.ofNullable(userRepository.findByUsername(transactionSearchFields.getUsername())
                .orElseThrow(() ->
                        new CustomException("User does not exist", HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase())));

        String userId = user.map(u -> String.valueOf(u.getUserId()))
                .orElseThrow(() -> new CustomException("Invalid user", HttpStatus.BAD_REQUEST, "Invalid user"));

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
