package com.yh.budgetly.service;

import com.yh.budgetly.entity.Transaction;
import com.yh.budgetly.entity.User;
import com.yh.budgetly.exceptions.CustomException;
import com.yh.budgetly.mapper.TransactionMapper;
import com.yh.budgetly.repository.CategoryRepository;
import com.yh.budgetly.repository.TransactionRepository;
import com.yh.budgetly.repository.UserRepository;
import com.yh.budgetly.rest.dtos.TransactionDTO;
import com.yh.budgetly.rest.requests.SearchFields;
import com.yh.budgetly.rest.requests.TransactionFields;
import com.yh.budgetly.rest.requests.TransactionRequestFields;
import com.yh.budgetly.rest.requests.TransactionSearchFields;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    public void testAddTransaction_UserNotFound() {
        String username = "NotFound";
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
                String.valueOf(mockUser.getUserId())
        )).thenReturn(mockTransactionList);

        when(transactionMapper.toTransactionDTOList(mockTransactionList))
                .thenReturn(mockTransactionDTOList);

        List<TransactionDTO> result =
                transactionService.transactionSummary("test");

        assertEquals(mockTransactionDTOList.size(), mockTransactionList.size());
        verify(transactionRepository, times(1)).findUserTransactionById(
                String.valueOf(mockUser.getUserId())
        );
        verify(transactionMapper, times(1)).toTransactionDTOList(mockTransactionList);
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
    @Test
    void testSearchTransactionPageSummary_Success() {
        // Arrange
        TransactionSearchFields searchFields = new TransactionSearchFields();
        searchFields.setUsername("testUser");

        SearchFields fields = new SearchFields();
        fields.setCategory("FOOD");
        fields.setAmount("100");
        searchFields.setSearchFields(fields);

        User mockUser = new User();
        mockUser.setUserId(1L);

        Transaction mockTransaction = new Transaction();
        TransactionDTO mockTransactionDTO = new TransactionDTO();

        Page<Transaction> mockPage = new PageImpl<>(Collections.singletonList(mockTransaction));
        Page<TransactionDTO> expectedDTOPage = new PageImpl<>(Collections.singletonList(mockTransactionDTO));


        // Mock repository calls
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
        when(transactionRepository.findUserTransactionSearchesByIdPagination(
                "1",
                "FOOD",
                100.0,
//                any(PageRequest.class)
                PageRequest.of(0,10)
        )).thenReturn(mockPage);

        when(transactionMapper.toTransactionDTOPage(mockPage)).thenReturn(expectedDTOPage);

        // Act
        Page<TransactionDTO> result = transactionService.searchTransactionPageSummary(searchFields, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDTOPage, result);
        verify(userRepository).findByUsername("testUser");
        verify(transactionRepository).findUserTransactionSearchesByIdPagination(
                ("1"),
                ("FOOD"),
                (100.0),
                PageRequest.of(0,10)
        );
    }
    @Test
    void testSearchTransactionPageSummary_UserNotFound() {
        // Arrange
        TransactionSearchFields searchFields = new TransactionSearchFields();
        searchFields.setUsername("nonexistentUser");

        SearchFields fields = new SearchFields();
        fields.setCategory("FOOD");
        fields.setAmount("100");
        searchFields.setSearchFields(fields);

        when(userRepository.findByUsername("nonexistentUser")).thenReturn(Optional.empty());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () ->
                transactionService.searchTransactionPageSummary(searchFields, 0, 10)
        );

        assertEquals("User does not exist", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
