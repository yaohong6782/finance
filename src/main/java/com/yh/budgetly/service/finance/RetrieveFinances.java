package com.yh.budgetly.service.finance;

import com.yh.budgetly.entity.Income;
import com.yh.budgetly.entity.Savings;
import com.yh.budgetly.entity.User;
import com.yh.budgetly.interfaces.ServiceHandler;
import com.yh.budgetly.mapper.IncomeMapper;
import com.yh.budgetly.mapper.SavingsMapper;
import com.yh.budgetly.repository.IncomeRepository;
import com.yh.budgetly.repository.SavingRepository;
import com.yh.budgetly.rest.dtos.IncomeDTO;
import com.yh.budgetly.rest.dtos.SavingsDTO;
import com.yh.budgetly.rest.dtos.UserDTO;
import com.yh.budgetly.rest.responses.finances.FinanceSetting;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@Qualifier("retrieveFinances")
public class RetrieveFinances implements ServiceHandler<FinanceSetting, UserDTO> {

    private final SavingRepository savingRepository;
    private final SavingsMapper savingsMapper;
    private final IncomeRepository incomeRepository;
    private final IncomeMapper incomeMapper;

    @Override
    public FinanceSetting retrieve(UserDTO userDTO) {
        User user = User.builder()
                .userId(userDTO.getUserId())
                .build();

        List<Savings> savings = savingRepository.findAllByUser(user);
        List<SavingsDTO> savingsDTOList = savingsMapper.incomeListToIncomeDTOList(savings);
        log.info("saving dto list : {}", savingsDTOList);

        List<Income> income = incomeRepository.findAllByUser(user);
        List<IncomeDTO> incomeDTOList = incomeMapper.incomeListToIncomeDTOList(income);
        log.info("income dto list : {}", incomeDTOList);

        BigDecimal currentMonthTotalIncome = totalIncomeForCurrentMonth(incomeDTOList);
        log.info("current month income : {} ", currentMonthTotalIncome);

        return FinanceSetting.builder()
                .incomeDTO(incomeDTOList)
                .savingsDTO(savingsDTOList)
                .totalIncomeSumCurrentMonth(String.valueOf(currentMonthTotalIncome))
                .build();
    }

    private BigDecimal totalIncome(List<IncomeDTO> incomeDTOList) {
        return incomeDTOList.stream()
                .map(IncomeDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalIncomeForCurrentMonth(List<IncomeDTO> incomeDTOList) {
//        LocalDate currentDate = LocalDate.now();
//        int currentYear = currentDate.getYear();
//        Month currentMonth = currentDate.getMonth();

        LocalDate currentDate = LocalDate.of(2025, Month.APRIL, 25);
        int currentYear = currentDate.getYear();
        Month currentMonth = currentDate.getMonth();
        log.info("current year : {}, current month : {} ", currentYear, currentMonth);
        log.info("current month bigdecimal function: {}", currentMonth);

        return incomeDTOList.stream()
                .filter(income -> isIncomeInSameMonth(income.getIncomeDate(), currentYear, currentMonth))
                .map(IncomeDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    boolean isIncomeInSameMonth(LocalDate incomeDate, int year, Month month) {
        return incomeDate != null && incomeDate.getYear() == year && incomeDate.getMonth() == month;
    }
}
