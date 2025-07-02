package com.yh.budgetly.service.finance;

import com.yh.budgetly.constants.CommonVariables;
import com.yh.budgetly.entity.Savings;
import com.yh.budgetly.entity.User;
import com.yh.budgetly.exceptions.CustomException;
import com.yh.budgetly.interfaces.ServiceHandler;
import com.yh.budgetly.mapper.SavingsMapper;
import com.yh.budgetly.mapper.UserMapper;
import com.yh.budgetly.repository.IncomeRepository;
import com.yh.budgetly.repository.SavingRepository;
import com.yh.budgetly.repository.UserRepository;
import com.yh.budgetly.rest.dtos.SavingsDTO;
import com.yh.budgetly.rest.dtos.UserDTO;
import com.yh.budgetly.rest.requests.UserFinanceGoals;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@AllArgsConstructor
@Qualifier("setFinanceGoals")
public class SetFinanceGoals implements ServiceHandler<SavingsDTO, UserFinanceGoals> {
    private final IncomeRepository incomeRepository;
    private final SavingRepository savingRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SavingsMapper savingsMapper;


    @Transactional
    @Override
    public SavingsDTO save(UserFinanceGoals userFinanceGoals) {
        log.info("Setting finance goals initialised");
        String expectedExpense = userFinanceGoals.getExpectedExpenses();
        String savingGoal = userFinanceGoals.getSavingGoals();
        String userFinanceMonthYear = userFinanceGoals.getMonthYear();

        User user = userRepository.findByUsername(userFinanceGoals.getUsername())
                .orElseThrow(() ->
                        new CustomException(CommonVariables.USER_NOT_FOUND, HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase()));

        UserDTO userDTO = userMapper.mapUserToUserDTO(user);

        Long userId = user.getUserId();

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        String currentFormattedDate = currentDate.format(formatter);
//        String currentFormattedDate = "07-2025";

        Savings savings  = savingRepository.findLatestSaving(userId, currentFormattedDate)
                .orElse(null);

        log.info("latest savings for current month : {} , savings : {} " , currentFormattedDate, savings);
        SavingsDTO savingsDTO = SavingsDTO.builder()
                .userDTO(userDTO)
                .monthYear(userFinanceMonthYear)
                .createdAt(LocalDate.now())
                .totalExpenses(expectedExpense)
                .savingsGoal(savingGoal)
                .build();


        if (savings != null) {
            log.info("saving is not null");
            String totalExpenses = savings.getTotalExpenses().toString();
            String savingsGoal = savings.getSavingsGoal().toString();

            if (expectedExpense.isBlank()) {
                expectedExpense = totalExpenses;
            }
            if (savingGoal.isBlank()) {
                savingGoal = savingsGoal;
            }
            log.info("user finance goals month year : {} and now month year : {} " ,
                    userFinanceGoals.getMonthYear(), currentFormattedDate);

            if (userFinanceGoals.getMonthYear().equals(currentFormattedDate)) {
                log.info("same month and year to be updated");
                savingsDTO.setUserDTO(userDTO);
                savingsDTO.setMonthYear(userFinanceMonthYear);
                savingsDTO.setCreatedAt(LocalDate.now());
                savingsDTO.setTotalExpenses(expectedExpense);
                savingsDTO.setSavingsGoal(savingGoal);

                Savings updatedSavings =  savingsMapper.savingsDTOToSavings(savingsDTO);

                try {
                    updatedSavings.setCreatedAt(LocalDate.now());
                    int saved = savingRepository.updatedSaveAndExpenseGoals(
                            updatedSavings.getTotalExpenses().toString(),
                            updatedSavings.getSavingsGoal().toString(),
                            updatedSavings.getCreatedAt(),
                            updatedSavings.getUser().getUserId(),
                            updatedSavings.getMonthYear()
                    );
                    log.info("successfully updated : {} " , saved);
                } catch (Exception e) {
                    log.error("Failed to update : {} ", e.getMessage());
                }
            } else {
                log.info("dates not tallying not updating");
            }

        } else {
            log.info("No savings found, creating new savings");
            Savings newSavings = savingsMapper.savingsDTOToSavings(savingsDTO);
            savingRepository.save(newSavings);
            log.info("Saving dto saved : {} " , savingsDTO);
        }

        return savingsDTO;
    }
}
