package com.yh.budgetly.service.finance;

import com.yh.budgetly.constants.CommonVariables;
import com.yh.budgetly.entity.Savings;
import com.yh.budgetly.entity.User;
import com.yh.budgetly.exceptions.CustomException;
import com.yh.budgetly.interfaces.ServiceHandler;
import com.yh.budgetly.mapper.SavingsMapper;
import com.yh.budgetly.repository.SavingRepository;
import com.yh.budgetly.repository.UserRepository;
import com.yh.budgetly.rest.dtos.SavingsDTO;
import com.yh.budgetly.rest.requests.UserFinanceGoals;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@Qualifier("viewFinanceGoals")
public class ViewFinanceGoals implements ServiceHandler<SavingsDTO, UserFinanceGoals> {

    private final SavingRepository savingRepository;
    private final UserRepository userRepository;
    private final SavingsMapper savingsMapper;

    @Override
    public SavingsDTO retrieve(UserFinanceGoals userFinanceGoals) {
        String username = userFinanceGoals.getUsername();

        log.info("Date to filter : {} ", userFinanceGoals.getMonthYear());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new CustomException(CommonVariables.USER_NOT_FOUND, HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase()));

        Long userId = user.getUserId();
        String monthYear = userFinanceGoals.getMonthYear();

        Savings savings = savingRepository.findLatestSavingWithoutMonthYear(userId, monthYear)
                .stream()
                .findFirst()
                .orElse(null);

        SavingsDTO savingsDTO = savingsMapper.savingsToSavingsDTO(savings);

        log.info("Returning savingsDTO : {} " , savingsDTO);

        return savingsDTO;
    }
}
