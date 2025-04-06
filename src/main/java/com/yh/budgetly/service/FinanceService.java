package com.yh.budgetly.service;

import com.yh.budgetly.constants.CommonVariables;
import com.yh.budgetly.entity.Income;
import com.yh.budgetly.entity.Savings;
import com.yh.budgetly.entity.User;
import com.yh.budgetly.exceptions.CustomException;
import com.yh.budgetly.mapper.SavingsMapper;
import com.yh.budgetly.mapper.IncomeMapper;
import com.yh.budgetly.mapper.UserMapper;
import com.yh.budgetly.repository.IncomeRepository;
import com.yh.budgetly.repository.SavingRepository;
import com.yh.budgetly.repository.UserRepository;
import com.yh.budgetly.rest.dtos.IncomeDTO;
import com.yh.budgetly.rest.dtos.SavingsDTO;
import com.yh.budgetly.rest.dtos.UserDTO;
import com.yh.budgetly.rest.requests.IncomeConfigurations;
import com.yh.budgetly.rest.requests.SavingConfigurations;
import com.yh.budgetly.rest.responses.finances.FinanceSetting;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;

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

    private LocalDate currentDate = LocalDate.now();
    private int currentYear = currentDate.getYear();
    private Month currentMonth = currentDate.getMonth();


    public IncomeDTO saveIncomeSettings(IncomeConfigurations incomeConfigurations) {
        if (incomeConfigurations.getIncomeDate() == null || incomeConfigurations.getIncomeDate().isBlank()) {
            ZonedDateTime currentDateTime = ZonedDateTime.now();
            ZonedDateTime incomeDate = currentDateTime.withDayOfMonth(25)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
            incomeConfigurations.setIncomeDate(incomeDate.toString());
        }
        log.info("Income configurations : {} ", incomeConfigurations);
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
        log.info("income : {} ", income);

        if (income.getSourceName().equalsIgnoreCase(CommonVariables.INCOME_SOURCE_CORPORATE_JOB)) {
            // Update corporate instead of creating new record
            log.info("found a corporate job");
            Optional<Income> existingIncome = incomeRepository.findBySourceName(CommonVariables.INCOME_SOURCE_CORPORATE_JOB);
            if (existingIncome.isPresent()) {
                log.info("it exist lets update it");
                Income incomeToUpdate = existingIncome.get();
                log.info("income to update : {} ", incomeToUpdate);

                BigDecimal incomeAmount = new BigDecimal(incomeConfigurations.getAmount());
                incomeToUpdate.setAmount(incomeAmount);

                LocalDateTime currentDate = LocalDateTime.now();
                LocalDateTime existingDate = incomeToUpdate.getIncomeDate();

                if (existingDate != null && (existingDate.getMonth() != currentDate.getMonth() || existingDate.getYear() != currentDate.getYear())) {
                    log.info("new income for the month");
                    incomeToUpdate.setIncomeDate(currentDate);
                } else {
                    log.info("theres no new income for the month");
                }

                Income updateSavedIncome = incomeRepository.save(incomeToUpdate);
            } else {
                log.info("saving a corporate job");
                Income savedIncome = incomeRepository.save(income);
                return incomeMapper.incomeToIncomeDTO(savedIncome);
            }
        }
        else {
            incomeRepository.save(income);
        }

        userDTO.setUserId(null);
        incomeDTO.setUserDTO(userDTO);

        return incomeDTO;
    }

    @Transactional
    public SavingsDTO saveSavingSetting(SavingConfigurations savingConfigurations) {

        log.info("save saving setting");

        User user = userRepository.findById(Long.valueOf(savingConfigurations.getUserId()))
                .orElseThrow(() ->
                        new CustomException(CommonVariables.USER_NOT_FOUND, HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase()));

        log.info("user : {} ", user);
        UserDTO userDTO = userMapper.mapUserToUserDTO(user);

        Long userTotalSavings = savingRepository.findUserTotalSavings(user);
        log.info("user total savings : {} ", userTotalSavings);

        YearMonth currentYearMonth = YearMonth.now();
        int month = currentYearMonth.getMonthValue();

        // YEAR = 2025, MONTH = 1 to have data for testing
        Long currentMonthExpenses = savingRepository.findCurrentMonthTotalExpenses(user, currentYear, 1);
        log.info("current month total expenses : {} ", currentMonthExpenses);

        List<Income> income = incomeRepository.findAllByUser(user);
        List<IncomeDTO> incomeDTOList = incomeMapper.incomeListToIncomeDTOList(income);
        BigDecimal currentTotalIncome = totalIncome(incomeDTOList);
        Long currentTotalIncomeLong = currentTotalIncome.longValue();

        log.info("save saving settings income dto list : {} ", incomeDTOList);
        log.info("current total income : {} ", currentTotalIncome);

        Long savingsSaved = currentTotalIncomeLong - currentMonthExpenses;
        log.info("total savings ; {} ", savingsSaved);

        SavingsDTO savingsDTO = SavingsDTO.builder()
                .userDTO(userDTO)
                .monthYear(String.valueOf(YearMonth.now()))
                .totalExpenses(String.valueOf(currentMonthExpenses))
                .createdAt(YearMonth.now().atEndOfMonth())
                .savingsAmount(String.valueOf(savingsSaved))
                .savingsGoal(savingConfigurations.getAmount())
                .build();

        Optional<Savings> existingSavings = savingRepository.findByUserIdAndMonthYear(user.getUserId(), String.valueOf(YearMonth.now()));

        if (existingSavings.isPresent()) {
            log.info("existing savings : {} ", existingSavings);
            Savings savingsToUpdate = existingSavings.get();

            log.info("savings to update monthyear : {} current month year : {}",
                    savingsToUpdate.getMonthYear(), YearMonth.now());

            if (savingsToUpdate.getMonthYear().equals(String.valueOf(YearMonth.now()))) {
                log.info("saving this date : {} ", String.valueOf(LocalDate.now()));
                int updatedSavings = savingRepository.updateSavings(
                        String.valueOf(savingsToUpdate.getTotalExpenses()),
                        String.valueOf(savingConfigurations.getAmount()),
                        LocalDate.now(),
                        savingsToUpdate.getUser().getUserId(),
                        savingsToUpdate.getMonthYear()
                );

                if (updatedSavings > 0) {
                    log.info("Successfully updated your savings for the month");
                      return SavingsDTO.builder()
                            .userDTO(userDTO)
                            .monthYear(String.valueOf(YearMonth.now()))
                            .totalExpenses(String.valueOf(savingsToUpdate.getTotalExpenses()))
                            .createdAt(LocalDate.now())
                            .savingsAmount(String.valueOf(savingConfigurations.getAmount()))
                            .savingsGoal(savingConfigurations.getAmount())
                            .build();
                } else {
                    log.info("No savings record found to update");
                }
            }
        } else {
            log.info("Saving new goals for new month");
            Savings savings = savingsMapper.savingsDTOToSavings(savingsDTO);
            log.info("attempting to save : {} ", savings);
            try {
                savingRepository.save(savings);
                log.info("Successfully recorded your new savings goals");
            } catch (Exception e) {
                log.error("Issue updating your savings");
            }
            log.info("savings : {} ", savings);
        }

        return savingsDTO;
    }

    public FinanceSetting financeSettings(UserDTO userDTO) {
        User user = new User();
        user.setUserId(userDTO.getUserId());

        List<Savings> savings = savingRepository.findAllByUser(user);
        List<SavingsDTO> savingsDTOList = savingsMapper.incomeListToIncomeDTOList(savings);
        ;
        log.info("retrieved savings {},  saving DTO list : {} ", savings, savingsDTOList);

        List<Income> income = incomeRepository.findAllByUser(user);
        List<IncomeDTO> incomeDTOList = incomeMapper.incomeListToIncomeDTOList(income);
        log.info("Finance settings Income DTO List : {} ", incomeDTOList);

        BigDecimal currentMonthTotalIncome = totalIncomeForCurrentMonth(incomeDTOList);
        log.info("current month income : {} ", currentMonthTotalIncome);
        BigDecimal totalIncome = totalIncome(incomeDTOList);
        log.info("total income : {} ", totalIncome);


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
