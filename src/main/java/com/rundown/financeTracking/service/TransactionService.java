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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
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

        for (TransactionFields transField : transactionRequestFields.getTransactionFields()) {
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

    public void transactionSummary(String username) {
        Optional<User> user = Optional.ofNullable(userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new CustomException("User does not exist", HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase())));

        String userId = "";
        if (user.isPresent()) {
            userId = String.valueOf(user.get().getUserId());
        }
        log.info("user id to find : {} " , userId);
        List<Transaction> transactionList = transactionRepository.findUserTransactionById(userId);
        log.info("transaction list : {} size : {}  " , transactionList.toString(), transactionList.size());
    }
}
