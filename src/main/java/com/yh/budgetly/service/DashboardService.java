package com.yh.budgetly.service;

import com.yh.budgetly.constants.CommonVariables;
import com.yh.budgetly.entity.Savings;
import com.yh.budgetly.entity.Transaction;
import com.yh.budgetly.entity.User;
import com.yh.budgetly.exceptions.CustomException;
import com.yh.budgetly.mapper.SavingsMapper;
import com.yh.budgetly.mapper.TransactionMapper;
import com.yh.budgetly.repository.*;
import com.yh.budgetly.rest.dtos.ExpenseTrendDTO;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
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
        LocalDate startCurrentMonth = LocalDate.now().withDayOfMonth(1);
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


        String monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        Optional<Savings> savings = savingRepository.findByUserIdAndMonthYear(user.getUserId(), monthYear);

        log.info("month year : {} ", monthYear);
        log.info("savings : {} ", savings);

        SavingsDTO savingsDTO = savings.map(savingsMapper::savingsToSavingsDTO).orElse(null);

        BigDecimal currentTotalSaved = monthlyMapFinanceDTO.values()
                .stream()
                .map(MonthlyFinanceDTO::getAmountSaved)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNetIncome = monthlyMapFinanceDTO.values()
                .stream()
                .map(MonthlyFinanceDTO::getIncome)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        log.info("current year : {} and current month : {} ", currentYear, currentMonth);


        BigDecimal totalMonthlyIncome = incomeRepository.findTotalIncomeThisMonth(userId, currentMonth, currentYear)
                .orElse(null);

        ExpenseTrendDTO expenseTrendDTO = expensesTrend(userId, currentMonthTotalExpenses, totalMonthlyIncome);

        return DashboardResponse.builder()
                .transactionDTO(null)
                .financeBreakDown(financeBreakDown)
                .monthlyFinanceDTO(monthlyMapFinanceDTO)
                .savingsDTO(savingsDTO)
                .currentMonthExpenses(currentMonthTotalExpenses)
                .currentNetSavings(currentTotalSaved)
                .currentNetIncome(totalNetIncome)
                .expenseTrendDTO(expenseTrendDTO)
                .build();
    }


    private Map<Integer, MonthlyFinanceDTO> monthlyFinanceDTOMap(
                String userId) {

        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        Map<Integer, BigDecimal> yearlyCreditCardPayments = fetchYearlyCreditCardPayments();
        Map<Integer, BigDecimal> yearlyDashboardSpending = fetchYearlySpending(userId, currentYear);
        Map<Integer, BigDecimal> yearlyDashboardIncome = fetchYearlyIncome(userId, currentYear);


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

    private ExpenseTrendDTO expensesTrend(String userId, BigDecimal currentMonthExpenses, BigDecimal currentMonthIncome) {
        YearMonth currentMonth = YearMonth.now();
        YearMonth prevMonth = currentMonth.minusMonths(1);
        LocalDate startOfPrevMonth = prevMonth.atDay(1);
        LocalDate endOfPrevMonth = prevMonth.atEndOfMonth();

        log.info("trend start of prev month : {} , end of prev month : {} ", startOfPrevMonth, endOfPrevMonth);

        BigDecimal prevMonthExpenses = transactionRepository.findGivenMonthTransaction(userId, startOfPrevMonth, endOfPrevMonth);
        log.info("current month expenses : {} ", currentMonthExpenses);
        log.info("previous month expenses : {} ", prevMonthExpenses);

        int prevMonthValue = prevMonth.getMonthValue();
        int currentYear = LocalDate.now().getYear();
        BigDecimal prevMonthIncome = incomeRepository.findTotalIncomeThisMonth(userId, prevMonthValue,currentYear)
                .orElse(BigDecimal.ZERO);

        log.info("current month income : {} ", currentMonthIncome);
        log.info("previous month income : {} ", prevMonthIncome);

        BigDecimal currentMonthSaving = currentMonthIncome.subtract(currentMonthExpenses);
        BigDecimal prevMonthSaving = prevMonthIncome.subtract(prevMonthExpenses);
        log.info("current month saving : {} ", currentMonthSaving);
        log.info("previous month saving : {} ", prevMonthSaving);

        // Percentage changes comparing between current and previous month
        BigDecimal expensePercentChange = percentageTrends(prevMonthExpenses, currentMonthExpenses);
        BigDecimal incomePercentageChange = percentageTrends(prevMonthIncome, currentMonthIncome);
        BigDecimal savingPercentageChange = percentageTrends(prevMonthSaving, currentMonthSaving);

        ExpenseTrendDTO expenseTrendDTO = ExpenseTrendDTO.builder()
                .currentMonthExpense(currentMonthExpenses)
                .previousMonthExpense(prevMonthExpenses)
                .incomeNetPercentage(incomePercentageChange)
                .expensesNetPercentage(expensePercentChange)
                .savingsNetPercentage(savingPercentageChange)
                .build();

        log.info("expense trend dto : {} ", expenseTrendDTO);
        return expenseTrendDTO;
    }

    public BigDecimal percentageTrends(BigDecimal prevMonthAmt, BigDecimal currentMonthAmt) {
        BigDecimal percentageChange = BigDecimal.ZERO;
        BigDecimal currAndPrevMonthDiff = (currentMonthAmt.subtract(prevMonthAmt));
        if (prevMonthAmt != null && currentMonthAmt.compareTo(BigDecimal.ZERO) != 0) {
            percentageChange = (currAndPrevMonthDiff.divide(prevMonthAmt, 6, RoundingMode.HALF_UP))
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return percentageChange;
    }
}
