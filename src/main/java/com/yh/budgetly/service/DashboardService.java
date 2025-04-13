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
import com.yh.budgetly.rest.responses.dashboard.MonthlyFinanceDTO;
import com.yh.budgetly.rest.responses.dashboard.MonthlyIncome;
import com.yh.budgetly.rest.responses.dashboard.MonthlyTotalSpent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

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
        LocalDate now = LocalDate.now();
        String username = request.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new CustomException(CommonVariables.USER_NOT_FOUND, HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase()));

        log.info("Found user : {}", user);

        String userId = String.valueOf(user.getUserId());
        List<Transaction> transaction = transactionRepository.findUserTransactionById(userId);

        List<TransactionDTO> transactionDTOList = transactionMapper.toTransactionDTOList(transaction);
        log.info("Transactions DTO size : {} ", transactionDTOList.size());

        Map<String, BigDecimal> financeBreakDown = new HashMap<>();
        for (TransactionDTO transactionDTO : transactionDTOList) {
            String categoryType = transactionDTO.getCategoriesDTO().getType();
            BigDecimal amount = transactionDTO.getAmount();

            financeBreakDown.put(
                    categoryType,
                    financeBreakDown.getOrDefault(categoryType, BigDecimal.ZERO).add(amount)
            );
        }

        // monthlySpendd
        List<MonthlyTotalSpent> monthlySpentTotalList = transactionRepository.findAllMonthAndTotalSpent(userId, now.getYear());
        log.info("monthly total list : {} ", monthlySpentTotalList);

        Map<Integer, BigDecimal> yearlyDashboardSpending = yearlyMonthlyMap(
                monthlySpentTotalList,
                m -> Integer.parseInt(m.getMonthNum()),
                MonthlyTotalSpent::getAmountSpent
        );
        log.info("yearly monthly spending dashboard analysis : {} ", yearlyDashboardSpending);
        List<MonthlyIncome> monthlyTotalIncomeList = incomeRepository.findAllMonthAndAllIncome(userId, now.getYear());

        Map<Integer, BigDecimal> yearlyDashboardIncome = yearlyMonthlyMap(monthlyTotalIncomeList,
                m -> Integer.parseInt(m.getMonthNum()),
                MonthlyIncome::getIncomeAmount
        );
        Map<Integer, BigDecimal> yearlyIncome = yearlyMonthlyMap(
                monthlyTotalIncomeList,
                m -> Integer.parseInt(m.getMonthNum()),
                MonthlyIncome::getIncomeAmount
        );
        log.info(monthlyTotalIncomeList.toString());
        log.info("yearly dashboard monthly total income list : {}", yearlyDashboardIncome);

        BigDecimal currentIncome = incomeRepository.findLatestIncome(userId)
                .orElse(null);

        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        BigDecimal totalMonthlyIncome = incomeRepository.findTotalIncomeThisMonth(userId, currentMonth, currentYear)
                .orElse(null);

        log.info("current income : {} ", currentIncome);
        for (Map.Entry<Integer, BigDecimal> entry : yearlyDashboardIncome.entrySet()) {
//            if (entry.getValue().compareTo(BigDecimal.ZERO) == 0 && entry.getKey() == now.getMonthValue()) {
//                yearlyDashboardIncome.put(entry.getKey(), currentIncome);
//            }
            if (entry.getValue().compareTo(BigDecimal.ZERO) == 0
                   && entry.getKey() == now.getMonthValue()) {

                Long sources = incomeRepository.countIncomeSourcesThisMonth(userId);
                log.info("sources : {}", sources);

                yearlyDashboardIncome.put(entry.getKey(), totalMonthlyIncome);
            }
        }
        log.info("updated monthly total income list : {}", yearlyDashboardIncome);

        /**
         * Months, Income and Expenses are in the map now
         * Find a way to return them in the API
         */

        Map<Integer, MonthlyFinanceDTO> monthlyFinanceDTOMap = new TreeMap<>();
        for (int month = 1; month <= 12; month++) {
            BigDecimal spent = yearlyDashboardSpending.getOrDefault(month, BigDecimal.ZERO);
            BigDecimal income = yearlyDashboardIncome.getOrDefault(month, BigDecimal.ZERO);
            monthlyFinanceDTOMap.put(month, new MonthlyFinanceDTO(spent, income));
        }
        log.info("monthly finance dto map : {} " , monthlyFinanceDTOMap);

        // Current month transaction
//        LocalDate startCurrentMonth = now.withDayOfMonth(1);
//        LocalDate startNextMonth = startCurrentMonth.plusMonths(1);
        LocalDate testDate = LocalDate.of(2025, 3, 4);
        LocalDate startCurrentMonth = testDate.withDayOfMonth(1);
        LocalDate startNextMonth = startCurrentMonth.plusMonths(1);
        log.info("start current month : {} , start next month : {} ", startCurrentMonth, startNextMonth);
        List<Transaction> currentMonthTransactions = transactionRepository
                .findCurrentMonthTransactions(userId, startCurrentMonth, startNextMonth);
        List<TransactionDTO> currentMonthTransactionDTO = transactionMapper.toTransactionDTOList(currentMonthTransactions);
        log.info("current month transaction dto size : {} ", currentMonthTransactionDTO.size());

        BigDecimal currentMonthTotalExpenses = currentMonthTransactionDTO.stream()
                .map(TransactionDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("current month total expenses : {} ", currentMonthTotalExpenses);

        // change customDate to now, LocalDate.now() after testing
        LocalDate customDate = LocalDate.of(2025, 3, 1);

        String monthYear = customDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        Optional<Savings> savings = savingRepository.findByUserIdAndMonthYear(user.getUserId(), monthYear);

        log.info("month year : {} ", monthYear);
        log.info("savings : {} ", savings);


        SavingsDTO savingsDTO = savings.map(savingsMapper::savingsToSavingsDTO).orElse(null);

        return DashboardResponse.builder()
                .transactionDTO(null)
                .financeBreakDown(financeBreakDown)
                .monthlyFinanceDTO(monthlyFinanceDTOMap)
                .savingsDTO(savingsDTO)
                .currentMonthExpenses(currentMonthTotalExpenses)
                .build();
    }

    private <T> Map<Integer, BigDecimal> yearlyMonthlyMap(
            List<T> monthlyDataList,
            Function<T, Integer> getMonth,
            Function<T, BigDecimal> getValue
    ) {
        Map<Integer, BigDecimal> monthMap = new TreeMap<>();
        for (int month = 1; month <= 12; month++) {
            monthMap.put(month, BigDecimal.ZERO);
        }
        for (T item : monthlyDataList) {
            monthMap.put(getMonth.apply(item), getValue.apply(item));
        }
        return monthMap;
    }
}
