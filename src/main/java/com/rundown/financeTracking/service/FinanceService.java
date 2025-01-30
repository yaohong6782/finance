package com.rundown.financeTracking.service;

import com.rundown.financeTracking.constants.CommonVariables;
import com.rundown.financeTracking.controller.FinancesController;
import com.rundown.financeTracking.entity.Income;
import com.rundown.financeTracking.entity.User;
import com.rundown.financeTracking.exceptions.CustomException;
import com.rundown.financeTracking.mapper.IncomeMapper;
import com.rundown.financeTracking.mapper.UserMapper;
import com.rundown.financeTracking.repository.IncomeRepository;
import com.rundown.financeTracking.repository.UserRepository;
import com.rundown.financeTracking.rest.dtos.IncomeDTO;
import com.rundown.financeTracking.rest.dtos.UserDTO;
import com.rundown.financeTracking.rest.requests.IncomeConfigurations;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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
    private IncomeMapper incomeMapper;

    @Autowired
    private UserMapper userMapper;

    public IncomeDTO saveIncomeSettings(IncomeConfigurations incomeConfigurations) {
        log.info("Income configurations : {} " , incomeConfigurations);
        User user = userRepository.findById(Long.valueOf(incomeConfigurations.getUserId()))
                .orElseThrow(() ->
                        new CustomException(CommonVariables.USER_NOT_FOUND, HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase()));

        log.info("User found : {} ", user);

        UserDTO userDTO = userMapper.mapUserToUserDTO(user);

        IncomeDTO incomeDTO = incomeMapper.incomeConfigurationToIncomeDTO(incomeConfigurations);
        incomeDTO.setUserDTO(userDTO);

        log.info("income dto : {} ", incomeDTO);

        Income income = incomeMapper.incomeDTOtoIncome(incomeDTO);
        log.info("income : {} " , income);

        Income savedIncome  = incomeRepository.save(income);

        return incomeDTO;
    }
}
