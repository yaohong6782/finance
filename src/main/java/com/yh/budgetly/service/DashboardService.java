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
import com.yh.budgetly.rest.responses.dashboard.*;
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
import java.util.stream.Collectors;

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

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());  // Last day of the current month

        Map<Integer, MonthlyFinanceDTO> monthlyMapFinanceDTO = monthlyFinanceDTOMap(userId);
        log.info("monthly finance dto map : {} " , monthlyMapFinanceDTO);

        // Current month transaction
//        LocalDate startCurrentMonth = now.withDayOfMonth(1);
//        LocalDate startNextMonth = startCurrentMonth.plusMonths(1);

        // TODO Testing purposes we are testing on April (4)
        LocalDate testDate = LocalDate.of(2025, 4, 4);
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
        // TODO Testing purposes we are testing on April (4)

        String monthYear = testDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        Optional<Savings> savings = savingRepository.findByUserIdAndMonthYear(user.getUserId(), monthYear);

        log.info("month year : {} ", monthYear);
        log.info("savings : {} ", savings);


        SavingsDTO savingsDTO = savings.map(savingsMapper::savingsToSavingsDTO).orElse(null);

        BigDecimal currentTotalSaved = monthlyMapFinanceDTO.values()
                .stream()
                .map(MonthlyFinanceDTO::getAmountSaved)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardResponse.builder()
                .transactionDTO(null)
                .financeBreakDown(financeBreakDown)
                .monthlyFinanceDTO(monthlyMapFinanceDTO)
                .savingsDTO(savingsDTO)
                .currentMonthExpenses(currentMonthTotalExpenses)
                .currentTotalSaved(currentTotalSaved)
                .build();
    }


    private Map<Integer, MonthlyFinanceDTO> monthlyFinanceDTOMap(
                String userId) {

        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        Map<Integer, BigDecimal> yearlyCreditCardPayments = fetchYearlyCreditCardPayments();
        Map<Integer, BigDecimal> yearlyDashboardSpending = fetchYearlySpending(userId, currentYear);
        Map<Integer, BigDecimal> yearlyDashboardIncome = fetchYearlyIncome(userId, currentYear);

        BigDecimal totalMonthlyIncome = incomeRepository.findTotalIncomeThisMonth(userId, currentMonth, currentYear)
                .orElse(null);

        for (Map.Entry<Integer, BigDecimal> entry : yearlyDashboardIncome.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) == 0
                    && entry.getKey() == LocalDate.now().getMonthValue()) {

                Long sources = incomeRepository.countIncomeSourcesThisMonth(userId);
                log.info("sources : {}", sources);

                yearlyDashboardIncome.put(entry.getKey(), totalMonthlyIncome);
            }
        }
        log.info("updated monthly total income list : {}", yearlyDashboardIncome);

        Map<Integer, MonthlyFinanceDTO> resultMap = new TreeMap<>();
        for (int month = 1; month <= 12; month++) {
            BigDecimal spent = yearlyDashboardSpending.getOrDefault(month, BigDecimal.ZERO);
            BigDecimal income = yearlyDashboardIncome.getOrDefault(month, BigDecimal.ZERO);
            BigDecimal creditCardPayment = yearlyCreditCardPayments.getOrDefault(month, BigDecimal.ZERO);
            BigDecimal amountSaved = income.subtract(spent);
            resultMap.put(month, new MonthlyFinanceDTO(spent, income, creditCardPayment, amountSaved));
        }
        return resultMap;
    }

    private Map<Integer, BigDecimal> fetchYearlyCreditCardPayments() {
        LocalDate startOfYear = LocalDate.now().withDayOfYear(1);
        LocalDate endOfYear = LocalDate.now().withDayOfYear(1).plusYears(1).minusDays(1);


        List<MonthlyCreditCardPaymentDTO> results = transactionRepository.getYearlyCreditCardPayments(
                "Credit Card", startOfYear, endOfYear
        );

        Map<Integer, BigDecimal> yearlyCreditCardMap = results.stream()
                .collect(Collectors.toMap(
                        MonthlyCreditCardPaymentDTO::getMonth,
                        MonthlyCreditCardPaymentDTO::getTotalAmount
                ));

        log.info("yearly credit card payments : {}", yearlyCreditCardMap);
        return yearlyCreditCardMap;
    }

    private Map<Integer, BigDecimal> fetchYearlySpending(String userId, int year) {

        List<MonthlyTotalSpent> monthlySpentTotalList = transactionRepository.findAllMonthAndTotalSpent(userId, LocalDate.now().getYear());
        log.info("monthly total list : {} ", monthlySpentTotalList);

        Map<Integer, BigDecimal> yearlyDashboardSpending = yearlyMonthlyMap(
                monthlySpentTotalList,
                m -> Integer.parseInt(m.getMonthNum()),
                MonthlyTotalSpent::getAmountSpent
        );
        log.info("yearly monthly spending dashboard analysis : {} ", yearlyDashboardSpending);

        return yearlyDashboardSpending;
    }

    private Map<Integer, BigDecimal> fetchYearlyIncome(String userId, int year) {
        List<MonthlyIncome> monthlyTotalIncomeList = incomeRepository.findAllMonthAndAllIncome(userId, year);
        Map<Integer, BigDecimal> yearlyDashboardIncome = yearlyMonthlyMap(monthlyTotalIncomeList,
                m -> Integer.parseInt(m.getMonthNum()),
                MonthlyIncome::getIncomeAmount
        );

        log.info("monthly total income list: {}", monthlyTotalIncomeList);
        log.info("yearly dashboard monthly total income list : {}", yearlyDashboardIncome);

        return yearlyDashboardIncome;
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
