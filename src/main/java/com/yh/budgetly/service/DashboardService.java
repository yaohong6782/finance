package com.yh.budgetly.service;

import com.yh.budgetly.constants.CommonVariables;
import com.yh.budgetly.entity.Savings;
import com.yh.budgetly.entity.Transaction;
import com.yh.budgetly.entity.User;
import com.yh.budgetly.exceptions.CustomException;
import com.yh.budgetly.mapper.SavingsMapper;
import com.yh.budgetly.mapper.TransactionMapper;
import com.yh.budgetly.repository.*;
import com.yh.budgetly.rest.dtos.SavingsDTO;
import com.yh.budgetly.rest.dtos.TransactionDTO;
import com.yh.budgetly.rest.requests.MandatoryFields;
import com.yh.budgetly.rest.responses.dashboard.DashboardResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Builder
@Service
@AllArgsConstructor
public class DashboardService {
    private final TransactionRepository transactionRepository;
    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final SavingRepository savingRepository;

    private final TransactionMapper transactionMapper;
    private final SavingsMapper savingsMapper;

    public DashboardResponse dashboardResponse(MandatoryFields request) {
        String username = request.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                    new CustomException(CommonVariables.USER_NOT_FOUND, HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase()));

        log.info("Found user : {}" , user);

        String userId = String.valueOf(user.getUserId());
        List<Transaction> transaction = transactionRepository
                .findUserTransactionById(userId);

        List<TransactionDTO> transactionDTOList = transactionMapper.toTransactionDTOList(transaction);
        log.info("Transactions DTO size : {} ", transactionDTOList.size());

        // Current month transaction
        LocalDate startCurrentMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate startNextMonth = startCurrentMonth.plusMonths(1);
//        LocalDate testDate = LocalDate.of(2024,12,4);
//        LocalDate startCurrentMonth = testDate.withDayOfMonth(1);
//        LocalDate startNextMonth = startCurrentMonth.plusMonths(1);
        log.info("start current month : {} , start next month : {} " ,startCurrentMonth , startNextMonth);
        List<Transaction> currentMonthTransactions = transactionRepository
                .findCurrentMonthTransactions(userId, startCurrentMonth, startNextMonth);
        List<TransactionDTO> currentMonthTransactionDTO = transactionMapper.toTransactionDTOList(currentMonthTransactions);
        log.info("current month transaction dto size : {} ", currentMonthTransactionDTO.size());

        BigDecimal currentMonthTotalExpenses = currentMonthTransactionDTO.stream()
                .map(TransactionDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("current month total expenses : {} " , currentMonthTotalExpenses);

        LocalDate now = LocalDate.now();
        LocalDate customDate = LocalDate.of(2025, 3, 1);
        String monthYear = customDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        Optional<Savings> savings = savingRepository.findByUserIdAndMonthYear(user.getUserId(), monthYear);

        log.info("month year : {} ", monthYear);
        log.info("savings : {} ", savings);


        SavingsDTO savingsDTO = savings.map(savingsMapper::savingsToSavingsDTO).orElse(null);

        DashboardResponse response = DashboardResponse.builder()
                .transactionDTO(transactionDTOList)
                .savingsDTO(savingsDTO)
                .currentMonthExpenses(currentMonthTotalExpenses)
                .build();

        return response;
    }
}
