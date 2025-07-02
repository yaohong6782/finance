package com.yh.budgetly.service.finance;

import com.yh.budgetly.constants.CommonVariables;
import com.yh.budgetly.entity.Income;
import com.yh.budgetly.entity.User;
import com.yh.budgetly.exceptions.CustomException;
import com.yh.budgetly.interfaces.ServiceHandler;
import com.yh.budgetly.mapper.IncomeMapper;
import com.yh.budgetly.repository.IncomeRepository;
import com.yh.budgetly.repository.UserRepository;
import com.yh.budgetly.rest.dtos.IncomeDTO;
import com.yh.budgetly.rest.requests.UserIncomeDetailsDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@Qualifier("retrieveFilteredIncome")
public class RetrieveFilteredIncome implements ServiceHandler<List<IncomeDTO>, UserIncomeDetailsDTO> {
    private final IncomeRepository incomeRepository;
    private final IncomeMapper incomeMapper;
    private final UserRepository userRepository;

    @Override
    public List<IncomeDTO> retrieve(UserIncomeDetailsDTO userIncomeDetailsDTO) {

        log.info("Retrieve filter income service initialised : {}", userIncomeDetailsDTO);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        // grabbing month and year from 05-2025
        YearMonth yearMonth = YearMonth.parse(userIncomeDetailsDTO.getMonthYear(), formatter);
        int month = yearMonth.getMonthValue();
        int year = yearMonth.getYear();

        String username = userIncomeDetailsDTO.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new CustomException(CommonVariables.USER_NOT_FOUND, HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase()));

        Long userId = user.getUserId();
        List<Income> filteredIncome = incomeRepository.findByMonthYear(String.valueOf(userId), month, year);
        List<IncomeDTO> filteredIncomeDTO = incomeMapper.incomeListToIncomeDTOList(filteredIncome);

        log.info("filtered income dto for : {} is : {} ", yearMonth, filteredIncomeDTO);

        return filteredIncomeDTO;
    }
}
