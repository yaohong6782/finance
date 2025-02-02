package com.rundown.financeTracking.service;

import com.rundown.financeTracking.constants.CommonVariables;
import com.rundown.financeTracking.entity.Income;
import com.rundown.financeTracking.entity.Savings;
import com.rundown.financeTracking.entity.User;
import com.rundown.financeTracking.exceptions.CustomException;
import com.rundown.financeTracking.mapper.SavingsMapper;
import com.rundown.financeTracking.mapper.IncomeMapper;
import com.rundown.financeTracking.mapper.UserMapper;
import com.rundown.financeTracking.repository.IncomeRepository;
import com.rundown.financeTracking.repository.SavingRepository;
import com.rundown.financeTracking.repository.UserRepository;
import com.rundown.financeTracking.rest.dtos.IncomeDTO;
import com.rundown.financeTracking.rest.dtos.SavingsDTO;
import com.rundown.financeTracking.rest.dtos.UserDTO;
import com.rundown.financeTracking.rest.requests.IncomeConfigurations;
import com.rundown.financeTracking.rest.responses.finances.FinanceSetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Builder
@Service
@AllArgsConstructor
@NoArgsConstructor
public class FinanceService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private SavingRepository savingRepository;

    @Autowired
    private IncomeMapper incomeMapper;

    @Autowired
    private SavingsMapper savingsMapper;

    @Autowired
    private UserMapper userMapper;

    public IncomeDTO saveIncomeSettings(IncomeConfigurations incomeConfigurations) {
        if (incomeConfigurations.getIncomeDate().isBlank()) {
            ZonedDateTime currentDateTime = ZonedDateTime.now();
            ZonedDateTime incomeDate = currentDateTime.withDayOfMonth(25)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
            incomeConfigurations.setIncomeDate(incomeDate.toString());
        }
        log.info("Income configurations : {} " , incomeConfigurations);
        User user = userRepository.findById(Long.valueOf(incomeConfigurations.getUserId()))
                .orElseThrow(() ->
                        new CustomException(CommonVariables.USER_NOT_FOUND, HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase()));

        log.info("User found : {} ", user);

        UserDTO userDTO = userMapper.mapUserToUserDTO(user);


        IncomeDTO incomeDTO = incomeMapper.incomeConfigurationToIncomeDTO(incomeConfigurations);
        incomeDTO.setUserDTO(userDTO);
        incomeDTO.setRecurring(incomeConfigurations.getSource().equalsIgnoreCase(CommonVariables.INCOME_SOURCE_CORPORATE_JOB));

        log.info("income dto : {} ", incomeDTO);

        Income income = incomeMapper.incomeDTOtoIncome(incomeDTO);
        log.info("income : {} " , income);

        Income savedIncome  = incomeRepository.save(income);

        userDTO.setUserId(null);
        incomeDTO.setUserDTO(userDTO);

        return incomeDTO;
    }

    public FinanceSetting financeSettings(UserDTO userDTO) {
        User user = new User();
        user.setUserId(userDTO.getUserId());

        List<Savings> savings = savingRepository.findAllByUser(user);
        List<SavingsDTO> savingsDTOList = savingsMapper.incomeListToIncomeDTOList(savings); ;
        log.info("retrieved savings {},  saving DTO list : {} ", savings, savingsDTOList);

        List<Income> income = incomeRepository.findAllByUser(user);
        List<IncomeDTO> incomeDTOList = incomeMapper.incomeListToIncomeDTOList(income);
        log.info("retrieved income : {} ", income);
        log.info("Income DTO List : {} ", incomeDTOList);

        BigDecimal currentMonthTotalIncome = totalIncomeForMonth(incomeDTOList);
        log.info("current month income : {} ", currentMonthTotalIncome);

        return FinanceSetting.builder()
                .incomeDTO(incomeDTOList)
                .savingsDTO(savingsDTOList)
                .totalSumCurrentMonth(String.valueOf(currentMonthTotalIncome))
                .build();
    }

    public BigDecimal totalIncomeForMonth(List<IncomeDTO> incomeDTOList) {
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        Month currentMonth = currentDate.getMonth();
        log.info("current month : {}" , currentMonth);

        return incomeDTOList.stream()
                .filter(income -> isIncomeInSameMonth(income.getIncomeDate(), currentYear, currentMonth))
                .map(IncomeDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isIncomeInSameMonth(LocalDate incomeDate, int year, Month month) {
        return incomeDate != null && incomeDate.getYear() == year && incomeDate.getMonth() == month;
    }
}
