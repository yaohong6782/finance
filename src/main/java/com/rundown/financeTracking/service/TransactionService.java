package com.rundown.financeTracking.service;

import com.rundown.financeTracking.entity.Categories;
import com.rundown.financeTracking.entity.Transaction;
import com.rundown.financeTracking.entity.User;
import com.rundown.financeTracking.repository.CategoryRepository;
import com.rundown.financeTracking.repository.TransactionRepository;
import com.rundown.financeTracking.rest.requests.TransactionFields;
import com.rundown.financeTracking.rest.requests.TransactionRequestFields;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public String addTransaction(TransactionRequestFields transactionRequestFields) {
        log.info("Processing transactions to be added : {} " , transactionRequestFields);

        String username = transactionRequestFields.getUser();
        User user = transactionRepository.findUserByName(username);
        log.info("User found : {} " , user);

        List<Transaction> transactionsList = new ArrayList<>();
        List<Categories> categoriesList = new ArrayList<>();

        for (TransactionFields t : transactionRequestFields.getTransactionFields()) {
            Categories categories = categoryRepository.findByType(t.getCategory().toLowerCase())
                    .orElseGet(() -> {
                Categories newCategory = new Categories();
                newCategory.setType(t.getCategory());

                categoriesList.add(newCategory);
                return newCategory;
            });
            Transaction transaction = Transaction.builder()
                    .user(user)
                    .categories(categories)
                    .amount(t.getAmount())
                    .transactionDate(LocalDate.parse(t.getTransactionDate()))
                    .description(t.getDescription())
                    .build();
            transactionsList.add(transaction);
        }

        if (!categoriesList.isEmpty()) {
           categoryRepository.saveAll(categoriesList);
        }

        transactionRepository.saveAll(transactionsList);

        return "Successfully added transaction please check";
    }
}
