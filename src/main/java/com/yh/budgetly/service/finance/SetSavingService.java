package com.yh.budgetly.service.finance;

import com.yh.budgetly.constants.CommonVariables;
import com.yh.budgetly.entity.Income;
import com.yh.budgetly.entity.Savings;
import com.yh.budgetly.entity.User;
import com.yh.budgetly.exceptions.CustomException;
import com.yh.budgetly.interfaces.ServiceHandler;
import com.yh.budgetly.mapper.IncomeMapper;
import com.yh.budgetly.mapper.SavingsMapper;
import com.yh.budgetly.mapper.UserMapper;
import com.yh.budgetly.repository.IncomeRepository;
import com.yh.budgetly.repository.SavingRepository;
import com.yh.budgetly.repository.UserRepository;
import com.yh.budgetly.rest.dtos.IncomeDTO;
import com.yh.budgetly.rest.dtos.SavingsDTO;
import com.yh.budgetly.rest.dtos.UserDTO;
import com.yh.budgetly.rest.requests.SavingConfigurations;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
@Qualifier("setSavingService")
public class SetSavingService implements ServiceHandler<SavingsDTO, SavingConfigurations> {
    private final UserRepository userRepository;
    private final SavingRepository savingRepository;
    private final IncomeRepository incomeRepository;

    private final IncomeMapper incomeMapper;
    private final SavingsMapper savingsMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public SavingsDTO save(SavingConfigurations request) {

        User user = userRepository.findById(Long.valueOf(request.getUserId()))
                .orElseThrow(() ->
                        new CustomException(CommonVariables.USER_NOT_FOUND, HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase()));

        UserDTO userDTO = userMapper.mapUserToUserDTO(user);
        log.info("User DTO : {} ", userDTO);

        YearMonth currentYearMonth = YearMonth.now();
        Long userId = userDTO.getUserId();

        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();

        Long currentMonthExpenses = savingRepository.findCurrentMonthTotalExpenses(user, currentYear, 1);

        List<Income> income = incomeRepository.findAllByUser(user);
        List<IncomeDTO> incomeDTOList = incomeMapper.incomeListToIncomeDTOList(income);

        BigDecimal currentTotalIncome = totalIncome(incomeDTOList);
        Long currentTotalIncomeLong = currentTotalIncome.longValue();
        Long savingsSaved = currentTotalIncomeLong - currentMonthExpenses;
        log.info("savings saved this month: {}", savingsSaved);


        SavingsDTO savingsDTO = SavingsDTO.builder()
                .userDTO(userDTO)
                .monthYear(String.valueOf(YearMonth.now()))
                .totalExpenses(String.valueOf(currentMonthExpenses))
                .createdAt(YearMonth.now().atEndOfMonth())
                .savingsAmount(String.valueOf(savingsSaved))
                .savingsGoal(request.getAmount())
                .build();

        Optional<Savings> existingSavings = savingRepository.findByUserIdAndMonthYear(userId, String.valueOf(currentYearMonth));
        if (existingSavings.isPresent()) {
            log.info("Existing saving exist for current month : {}", existingSavings);
            Savings savingsToUpdate = existingSavings.get();

            log.info("savings to update monthyear : {} current month year : {}",
                    savingsToUpdate.getMonthYear(), YearMonth.now());

            if (savingsToUpdate.getMonthYear().equals(String.valueOf(YearMonth.now()))) {
                log.info("saving this date : {} ", String.valueOf(LocalDate.now()));
                int updatedSavings = savingRepository.updateSavings(
                        String.valueOf(savingsToUpdate.getTotalExpenses()),
                        String.valueOf(request.getAmount()),
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
                            .savingsAmount(String.valueOf(request.getAmount()))
                            .savingsGoal(request.getAmount())
                            .build();
                } else {
                    log.info("No savings record found to update");
                }
            }
        } else {
            log.info("No existing saving exist for current month, create new one");
            Savings savings = savingsMapper.savingsDTOToSavings(savingsDTO);
            try {
                savingRepository.save(savings);
                log.info("Successfully recorded your new savings goals");
            } catch (Exception e) {
                log.error("Error saving savings");
            }
        }
        return savingsDTO;
    }

    private BigDecimal totalIncome(List<IncomeDTO> incomeDTOList) {
        return incomeDTOList.stream()
                .map(IncomeDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
