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
import com.rundown.financeTracking.rest.requests.SearchFields;
import com.rundown.financeTracking.rest.requests.TransactionFields;
import com.rundown.financeTracking.rest.requests.TransactionRequestFields;
import com.rundown.financeTracking.rest.requests.TransactionSearchFields;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private UserRepository userRepository;

    @Mock
    private UserService userService;

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

    @Test
    public void testListSummaryTransaction() {
        User mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setUsername("test");

        List<Transaction> mockTransactionList = new ArrayList<>(
                Arrays.asList(
                        new Transaction(),
                        new Transaction()
                )
        );

        List<TransactionDTO> mockTransactionDTOList = new ArrayList<>(
                Arrays.asList(
                        new TransactionDTO(),
                        new TransactionDTO()
                )
        );


        when(userRepository.findByUsername("test")).thenReturn(
                Optional.of(mockUser)
        );

        PageRequest pageRequest = PageRequest.of(0,10);
        when(transactionRepository.findUserTransactionById(
                String.valueOf(mockUser.getUserId()),
                pageRequest
        )).thenReturn(mockTransactionList);

        when(transactionMapper.toTransactionDTOList(mockTransactionList))
                .thenReturn(mockTransactionDTOList);

        List<TransactionDTO> result =
                transactionService.transactionSummary("test");



        assertEquals(mockTransactionDTOList.size(), mockTransactionList.size());
        verify(transactionRepository, times(1)).findUserTransactionById(
                String.valueOf(mockUser.getUserId()),
                pageRequest
        );
        verify(transactionMapper, times(1)).toTransactionDTOList(mockTransactionList);
    }

    @Test
    public void testPaginatedSummaryTransaction() {
        User mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setUsername("test");


        when(userRepository.findByUsername("test"))
                .thenReturn(Optional.of(mockUser));

        Optional<User> user = userRepository.findByUsername("test");
        String userId = String.valueOf(user.get().getUserId());

        List<Transaction> mockTransactions = Arrays.asList(
                new Transaction(),
                new Transaction()
        );

        PageRequest pageRequest = PageRequest.of(0,10);
        Page<Transaction> mockTransactionPage = new PageImpl<>(
                mockTransactions,
                pageRequest,
                mockTransactions.size()
        );

        List<TransactionDTO> mockDTOs = Arrays.asList(
                new TransactionDTO(),
                new TransactionDTO()
        );

        Page<TransactionDTO> expectedDTOPage = new PageImpl<>(
                mockDTOs,
                pageRequest,
                mockDTOs.size()
        );

        when(transactionRepository.findUserTransactionByIdPagination(
                String.valueOf(mockUser.getUserId()),
                pageRequest
        )).thenReturn(mockTransactionPage);

        when(transactionMapper.toTransactionDTOPage(mockTransactionPage))
                .thenReturn(expectedDTOPage);

        Page<TransactionDTO> result =
                transactionService.transactionPageSummary("test", 0, 10);


        assertEquals("1", userId);
        assertEquals(expectedDTOPage, result);
        assertEquals(mockDTOs.size(), result.getContent().size());
    }

    @Test
    public void testSearchTransactionPageSummary() {
        User mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setUsername("test");

        SearchFields searchFields = new SearchFields();
        searchFields.setCategory("rent");
        searchFields.setAmount("200");

        TransactionSearchFields transactionSearchFields = new TransactionSearchFields();
        transactionSearchFields.setUsername("test");
        transactionSearchFields.setSearchFields(searchFields);

        List<Transaction> mockTransactions = Arrays.asList(
                new Transaction(),
                new Transaction()
        );

        PageRequest pageRequest = PageRequest.of(0,10);
        Page<Transaction> mockTransactionPage = new PageImpl<>(
                mockTransactions,
                pageRequest,
                mockTransactions.size()
        );

        List<TransactionDTO> mockDTOs = Arrays.asList(
                new TransactionDTO(),
                new TransactionDTO()
        );

        Page<TransactionDTO> expectedDTOPage = new PageImpl<>(
                mockDTOs,
                pageRequest,
                mockDTOs.size()
        );

        when(userRepository.findByUsername("test")).thenReturn(
                Optional.of(mockUser)
        );

        when(transactionRepository.findUserTransactionSearchesByIdPagination(
                String.valueOf(mockUser.getUserId()),
                "rent",
                Double.valueOf("200"),
                pageRequest
        )).thenReturn(mockTransactionPage);

        when(transactionMapper.toTransactionDTOPage(mockTransactionPage))
                .thenReturn(expectedDTOPage);

        Page<TransactionDTO> result =
                transactionService.searchTransactionPageSummary(
                        transactionSearchFields, 0, 10
                );
        assertEquals(expectedDTOPage, result);
    }
}
