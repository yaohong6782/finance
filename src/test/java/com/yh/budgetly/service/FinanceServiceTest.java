package com.yh.budgetly.service;

import com.yh.budgetly.constants.CommonVariables;
import com.yh.budgetly.entity.Income;
import com.yh.budgetly.entity.Savings;
import com.yh.budgetly.entity.User;
import com.yh.budgetly.exceptions.CustomException;
import com.yh.budgetly.mapper.IncomeMapper;
import com.yh.budgetly.mapper.SavingsMapper;
import com.yh.budgetly.mapper.TransactionMapper;
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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class FinanceServiceTest {

}
