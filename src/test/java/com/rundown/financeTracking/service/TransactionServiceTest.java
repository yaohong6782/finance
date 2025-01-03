package com.rundown.financeTracking.service;

import com.rundown.financeTracking.entity.Categories;
import com.rundown.financeTracking.entity.Transaction;
import com.rundown.financeTracking.entity.User;
import com.rundown.financeTracking.mapper.TransactionMapper;
import com.rundown.financeTracking.repository.CategoryRepository;
import com.rundown.financeTracking.repository.TransactionRepository;
import com.rundown.financeTracking.rest.requests.TransactionFields;
import com.rundown.financeTracking.rest.requests.TransactionRequestFields;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    public void testAddTransaction() {
        User mockUser = new User();
        mockUser.setUsername("test");
        when(transactionRepository.findUserByName(any()))
                .thenReturn(mockUser);

        TransactionFields transactionField = new TransactionFields();
        transactionField.setCategory("food");
        transactionField.setAmount(BigDecimal.valueOf(100.0));
        transactionField.setTransactionDate("2024-01-01");
        transactionField.setDescription("Lunch");

        TransactionRequestFields transactionRequestFields = new TransactionRequestFields();
        transactionRequestFields.setUser("test");
        transactionRequestFields.setTransactions(List.of(transactionField));

        Categories categories = new Categories();
        categories.setType("food");

        when(categoryRepository.findByType("food")).thenReturn(Optional.empty());

        transactionService.addTransaction(transactionRequestFields);

        verify(transactionRepository, times(1)).saveAll(anyList());
        verify(categoryRepository, times(1)).saveAll(anyList());
    }


}
