package com.rundown.budgetTracking.service;

import com.rundown.budgetTracking.constants.CommonVariables;
import com.rundown.budgetTracking.entity.Income;
import com.rundown.budgetTracking.entity.User;
import com.rundown.budgetTracking.exceptions.CustomException;
import com.rundown.budgetTracking.mapper.IncomeMapper;
import com.rundown.budgetTracking.mapper.UserMapper;
import com.rundown.budgetTracking.repository.IncomeRepository;
import com.rundown.budgetTracking.repository.UserRepository;
import com.rundown.budgetTracking.rest.dtos.IncomeDTO;
import com.rundown.budgetTracking.rest.dtos.UserDTO;
import com.rundown.budgetTracking.rest.requests.IncomeRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

@Slf4j
@AllArgsConstructor
@Service
public class IncomeService {
    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final IncomeMapper incomeMapper;

    public IncomeDTO userSalaryIncome(IncomeRequest incomeRequest) {

        IncomeDTO incomeDTO = incomeMapper.toIncomeDTO(incomeRequest);
        Long userId = incomeRequest.getUserId();


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Please sign in before inserting your income", HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase()));

        String incomeSource = incomeRequest.getSource();

        // If source is day job or if its blank, default it to monthly frequency
        // Set the start and end day to the first and last day of each month as well
        if (incomeSource.equalsIgnoreCase(CommonVariables.SOURCE_DAY_JOB) ||
                StringUtils.isBlank(incomeSource)) {
            incomeDTO.setFrequency(CommonVariables.SOURCE_MONTHLY);

            // Set the start date to the 1st of the current month
            LocalDate startDate = LocalDate.now().withDayOfMonth(1);
            incomeDTO.setStartDate(startDate);

            // Get the last day of the current month
            YearMonth currentMonth = YearMonth.now();
            LocalDate endDate = currentMonth.atEndOfMonth();
            incomeDTO.setEndDate(endDate);
        }

        Income income = incomeMapper.IncomeDTOtoIncome(incomeDTO);
        income.setUser(user);
        log.info("Income to be saved to db : {}", income);

        // Return as json response
        UserDTO userDTO = userMapper.mapUserToUserDTO(user);
        log.info("User DTO mapstruct : {} ", userDTO);

        incomeDTO.setUserDTO(userDTO);
        log.info("IncomeDTO mapstructed : {}" , incomeDTO);

        return incomeDTO;
    }
}
