package com.rundown.financeTracking.service;

import com.rundown.financeTracking.constants.CommonVariables;
import com.rundown.financeTracking.entity.Income;
import com.rundown.financeTracking.entity.Savings;
import com.rundown.financeTracking.entity.User;
import com.rundown.financeTracking.exceptions.CustomException;
import com.rundown.financeTracking.mapper.IncomeMapper;
import com.rundown.financeTracking.mapper.SavingsMapper;
import com.rundown.financeTracking.mapper.UserMapper;
import com.rundown.financeTracking.repository.IncomeRepository;
import com.rundown.financeTracking.repository.SavingRepository;
import com.rundown.financeTracking.repository.UserRepository;
import com.rundown.financeTracking.rest.dtos.IncomeDTO;
import com.rundown.financeTracking.rest.dtos.SavingsDTO;
import com.rundown.financeTracking.rest.dtos.UserDTO;
import com.rundown.financeTracking.rest.requests.IncomeConfigurations;
import com.rundown.financeTracking.rest.requests.SavingConfigurations;
import com.rundown.financeTracking.rest.responses.finances.FinanceSetting;
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

    @Mock
    private UserRepository userRepository;

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private SavingRepository savingRepository;

    @Mock
    private IncomeMapper incomeMapper;

    @Mock
    private SavingsMapper savingsMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private FinanceService financeService;

    @Test
    public void testSaveIncomeSettings_success2() {
        // Mock data
        User mockUser = User.builder()
                .userId(1L)
                .username("test")
                .build();

        IncomeConfigurations incomeConfigurations = IncomeConfigurations.builder()
                .userId(String.valueOf(1L))
                .amount("100")
                .incomeDate("2024-12-12")
                .description("test")
                .source(CommonVariables.INCOME_SOURCE_CORPORATE_JOB) // Updated source to trigger the new condition
                .build();

        UserDTO mockUserDTO = new UserDTO();
        mockUserDTO.setUserId(1L);
        mockUserDTO.setUsername("test");

        IncomeDTO mockIncomeDTO = new IncomeDTO();
        mockIncomeDTO.setUserDTO(mockUserDTO);

        Income mockIncome = new Income();
        mockIncome.setSourceName(CommonVariables.INCOME_SOURCE_CORPORATE_JOB); // Updated source name
        mockIncome.setUser(mockUser);

        Income existingIncome = new Income();
        existingIncome.setSourceName(CommonVariables.INCOME_SOURCE_CORPORATE_JOB);
        existingIncome.setUser(mockUser);
        existingIncome.setAmount(new BigDecimal("50")); // Existing amount

        Income updatedIncome = new Income();
        updatedIncome.setSourceName(CommonVariables.INCOME_SOURCE_CORPORATE_JOB);
        updatedIncome.setUser(mockUser);
        updatedIncome.setAmount(new BigDecimal("100")); // Updated amount

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(mockUser));
        when(userMapper.mapUserToUserDTO(mockUser))
                .thenReturn(mockUserDTO);
        when(incomeMapper.incomeConfigurationToIncomeDTO(incomeConfigurations))
                .thenReturn(mockIncomeDTO);
        when(incomeMapper.incomeDTOtoIncome(mockIncomeDTO))
                .thenReturn(mockIncome);
        when(incomeRepository.findBySourceName(CommonVariables.INCOME_SOURCE_CORPORATE_JOB))
                .thenReturn(Optional.of(existingIncome)); // Mock existing income
        when(incomeRepository.save(existingIncome)) // Mock saving the updated income
                .thenReturn(updatedIncome);

        IncomeDTO result = financeService.saveIncomeSettings(incomeConfigurations);

        assertEquals(mockIncomeDTO, result);

        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, times(1)).mapUserToUserDTO(mockUser);
        verify(incomeMapper, times(1)).incomeConfigurationToIncomeDTO(incomeConfigurations);
        verify(incomeMapper, times(1)).incomeDTOtoIncome(mockIncomeDTO);
        verify(incomeRepository, times(1)).findBySourceName(CommonVariables.INCOME_SOURCE_CORPORATE_JOB);
        verify(incomeRepository, times(1)).save(existingIncome);
    }

    @Test
    public void testSaveIncomeSettings_success() {
        User mockUser = User.builder()
                .userId(1L)
                .username("test")
                .build();

        IncomeConfigurations incomeConfigurations = IncomeConfigurations.builder()
                .userId(String.valueOf(1L))
                .amount("100")
                .incomeDate("2024-12-12")
                .description("test")
                .source("work")
                .build();

        UserDTO mockUserDTO = new UserDTO();
        mockUserDTO.setUserId(1L);
        mockUserDTO.setUsername("test");

        IncomeDTO mockIncomeDTO = new IncomeDTO();
        mockIncomeDTO.setUserDTO(mockUserDTO);

        Income mockIncome = new Income();
        mockIncome.setSourceName("test");
        mockIncome.setUser(mockUser);


        when(userRepository.findById(1L))
                .thenReturn(Optional.of(mockUser));
        when(userMapper.mapUserToUserDTO(mockUser))
                .thenReturn(mockUserDTO);
        when(incomeMapper.incomeConfigurationToIncomeDTO(incomeConfigurations))
                .thenReturn(mockIncomeDTO);
        when(incomeMapper.incomeDTOtoIncome(mockIncomeDTO))
                .thenReturn(mockIncome);
        when(incomeRepository.save(mockIncome)).thenReturn(mockIncome);
        IncomeDTO result = financeService.saveIncomeSettings(incomeConfigurations);

        log.info("Result IncomeDTO: {}", result);
        log.info("Mock IncomeDTO: {}", mockIncomeDTO);
        assertEquals(mockIncomeDTO, result);
        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, times(1)).mapUserToUserDTO(mockUser);
        verify(incomeMapper, times(1)).incomeConfigurationToIncomeDTO(incomeConfigurations);
        verify(incomeMapper, times(1)).incomeDTOtoIncome(mockIncomeDTO);
        verify(incomeRepository, times(1)).save(mockIncome);
    }


    @ParameterizedTest
    @MethodSource("provideIncomeTestCases")
    void testSaveIncomeSettings(String testCase,
                                String source,
                                String amount,
                                boolean isExistingCorporate,
                                BigDecimal existingAmount) {
        // Set up common mock objects
        User mockUser = User.builder()
                .userId(1L)
                .username("test")
                .build();

        UserDTO mockUserDTO = new UserDTO();
        mockUserDTO.setUserId(1L);
        mockUserDTO.setUsername("test");

        // Build test configuration
        IncomeConfigurations incomeConfig = IncomeConfigurations.builder()
                .userId(String.valueOf(1L))
                .amount(amount)
                .incomeDate("2024-12-12")
                .description("test")
                .source(source)
                .build();

        // Set up DTOs and entities
        IncomeDTO mockIncomeDTO = new IncomeDTO();
        mockIncomeDTO.setUserDTO(mockUserDTO);
        mockIncomeDTO.setSourceName(source);

        Income mockIncome = new Income();
        mockIncome.setSourceName(source);
        mockIncome.setUser(mockUser);
        mockIncome.setAmount(new BigDecimal(amount));

        // Common mocks
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userMapper.mapUserToUserDTO(mockUser)).thenReturn(mockUserDTO);
        when(incomeMapper.incomeConfigurationToIncomeDTO(incomeConfig)).thenReturn(mockIncomeDTO);
        when(incomeMapper.incomeDTOtoIncome(mockIncomeDTO)).thenReturn(mockIncome);

        // Conditional mocks based on test case
        if (isExistingCorporate) {
            Income existingIncome = new Income();
            existingIncome.setSourceName(source);
            existingIncome.setAmount(existingAmount);
            existingIncome.setUser(mockUser);

            when(incomeRepository.findBySourceName(CommonVariables.INCOME_SOURCE_CORPORATE_JOB))
                    .thenReturn(Optional.of(existingIncome));
        }

        when(incomeRepository.save(any(Income.class))).thenReturn(mockIncome);

        // Execute test
        IncomeDTO result = financeService.saveIncomeSettings(incomeConfig);

        // Verify result
        assertEquals(mockIncomeDTO, result);

        // Verify common interactions
        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, times(1)).mapUserToUserDTO(mockUser);
        verify(incomeMapper, times(1)).incomeConfigurationToIncomeDTO(incomeConfig);
        verify(incomeMapper, times(1)).incomeDTOtoIncome(mockIncomeDTO);

        // Verify conditional interactions
        if (isExistingCorporate) {
            verify(incomeRepository).findBySourceName(CommonVariables.INCOME_SOURCE_CORPORATE_JOB);
        }
        verify(incomeRepository).save(any(Income.class));
    }

    private static Stream<Arguments> provideIncomeTestCases() {
        return Stream.of(
                Arguments.of(
                        "Regular Income",
                        "work",
                        "100",
                        false,
                        new BigDecimal("100")
                ),
                Arguments.of(
                        "Corporate Job Income",
                        CommonVariables.INCOME_SOURCE_CORPORATE_JOB,
                        "200",
                        true,
                        new BigDecimal("100")
                )
                // You can add more test cases here easily
        );
    }

    @Test
    public void testSaveIncomeSettings_UserNotFound() {
        Long userId = 1L;
        IncomeConfigurations incomeConfigurations = new IncomeConfigurations();
        incomeConfigurations.setIncomeDate("");
        incomeConfigurations.setUserId(userId.toString());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            financeService.saveIncomeSettings(incomeConfigurations);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    public void testRetrieveFinanceSettings() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(1L);

        User mockUser = new User();
        mockUser.setUserId(userDTO.getUserId());

        List<Savings> mockSavings = new ArrayList<>();
        List<SavingsDTO> mockSavingsDTO = new ArrayList<>();

        List<Income> mockIncome = new ArrayList<>();
        List<IncomeDTO> mockIncomeDTO = new ArrayList<>();

        when(savingRepository.findAllByUser(mockUser)).thenReturn(mockSavings);
        when(savingsMapper.incomeListToIncomeDTOList(mockSavings)).thenReturn(mockSavingsDTO);

        when(incomeRepository.findAllByUser(mockUser)).thenReturn(mockIncome);
        when(incomeMapper.incomeListToIncomeDTOList(mockIncome)).thenReturn(mockIncomeDTO);

        FinanceSetting mockResponse = FinanceSetting.builder()
                .incomeDTO(mockIncomeDTO)
                .savingsDTO(mockSavingsDTO)
                .totalSumCurrentMonth(String.valueOf(0))
                .build();
        FinanceSetting actualResponse = financeService.financeSettings(userDTO);

        verify(savingRepository, times(1)).findAllByUser(mockUser);
        verify(savingsMapper, times(1)).incomeListToIncomeDTOList(mockSavings);

        verify(incomeRepository, times(1)).findAllByUser(mockUser);
        verify(incomeMapper, times(1)).incomeListToIncomeDTOList(mockIncome);

        assertEquals(mockResponse, actualResponse);
    }


    @Test
    public void testFinanceSettings_TotalIncomeForCurrentMonth() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(1L);

        User mockUser = new User();
        mockUser.setUserId(userDTO.getUserId());

        // Mock savings data
        List<Savings> mockSavings = new ArrayList<>();
        List<SavingsDTO> mockSavingsDTO = new ArrayList<>();

        // Mock income data
        LocalDate currentDate = LocalDate.now();
        Income income1 = new Income();
        income1.setAmount(new BigDecimal("1000"));
        income1.setIncomeDate(currentDate.atStartOfDay()); // Current month

        Income income2 = new Income();
        income2.setAmount(new BigDecimal("500"));
        income2.setIncomeDate(currentDate.minusMonths(1).atStartOfDay()); // Previous month

        List<Income> mockIncome = Arrays.asList(income1, income2);

        IncomeDTO incomeDTO1 = new IncomeDTO();
        incomeDTO1.setAmount(new BigDecimal("1000"));
        incomeDTO1.setIncomeDate(currentDate);

        IncomeDTO incomeDTO2 = new IncomeDTO();
        incomeDTO2.setAmount(new BigDecimal("500"));
        incomeDTO2.setIncomeDate(currentDate.minusMonths(1));

        List<IncomeDTO> mockIncomeDTO = Arrays.asList(incomeDTO1, incomeDTO2);

        // Mock repository and mapper behavior
        when(savingRepository.findAllByUser(mockUser)).thenReturn(mockSavings);
        when(savingsMapper.incomeListToIncomeDTOList(mockSavings)).thenReturn(mockSavingsDTO);

        when(incomeRepository.findAllByUser(mockUser)).thenReturn(mockIncome);
        when(incomeMapper.incomeListToIncomeDTOList(mockIncome)).thenReturn(mockIncomeDTO);

        // Act
        FinanceSetting result = financeService.financeSettings(userDTO);

        // Assert
        assertEquals(new BigDecimal("1000"), new BigDecimal(result.getTotalSumCurrentMonth())); // Only income1 should be counted for current month
        verify(savingRepository, times(1)).findAllByUser(mockUser);
        verify(incomeRepository, times(1)).findAllByUser(mockUser);
    }

    @Test
    public void testIncomeIsInSameMonth() {
        LocalDate mockIncomeDate = LocalDate.of(2025, 2, 5);
        boolean result = financeService.isIncomeInSameMonth(mockIncomeDate, 2025, Month.FEBRUARY);
        assertTrue(result, "Expected income date to be in the same month");
    }

    @Test
    public void testIncomeDateIsNull() {
        boolean result = financeService.isIncomeInSameMonth(null, 2025, Month.FEBRUARY);
        assertFalse(result, "Expected false when income date is null");
    }

    @Test
    public void testIncomeYearIsDifferent() {
        LocalDate mockIncomeDate = LocalDate.of(2024, 2, 5);
        boolean result = financeService.isIncomeInSameMonth(mockIncomeDate, 2025, Month.FEBRUARY);
        assertFalse(result, "Expected false when income year is different");
    }

    @Test
    public void testIncomeMonthIsDifferent() {
        LocalDate mockIncomeDate = LocalDate.of(2025, 3, 5);
        boolean result = financeService.isIncomeInSameMonth(mockIncomeDate, 2025, Month.FEBRUARY);
        assertFalse(result, "Expected false when income month is different");
    }

    @Test
    public void testSaveSettings_success() {
        SavingConfigurations savingConfigurations = new SavingConfigurations();
        savingConfigurations.setUserId("1");
        savingConfigurations.setAmount(String.valueOf(1000));

        User user = User.builder()
                .userId(1L)
                .build();

        Long userSavings = 100L;

        YearMonth currentMonth = YearMonth.now();  // Keep this consistent
        String currentMonthString = currentMonth.toString();

        Savings existingSavings = Savings.builder()
                .user(user)
                .monthYear(currentMonthString)
                .createdAt(LocalDate.of(2025,3,5))
                .totalExpenses(new BigDecimal(5000L))
                .build();


        when(userRepository.findById(Long.valueOf(savingConfigurations.getUserId()))).thenReturn(Optional.of(user));
        when(savingRepository.findUserTotalSavings(user)).thenReturn(userSavings);

        when(savingRepository.findByUserIdAndMonthYear(1L, currentMonthString))
                .thenReturn(Optional.of(existingSavings));

        when(savingRepository.updateSavings(
                String.valueOf(existingSavings.getTotalExpenses()),
                savingConfigurations.getAmount(),
                existingSavings.getCreatedAt(),
                1L,
                existingSavings.getMonthYear()
        )).thenReturn(1);

        SavingsDTO savingsDTO = financeService.saveSavingSetting(savingConfigurations);


        verify(userRepository, times(1)).findById(1L);
        verify(savingRepository).findUserTotalSavings(user);
        verify(savingRepository, times(1)).findByUserIdAndMonthYear(1L, String.valueOf(YearMonth.now()));
        verify(savingRepository, times(1)).updateSavings(
                String.valueOf(existingSavings.getTotalExpenses()),
                savingConfigurations.getAmount(),
                existingSavings.getCreatedAt(),
                1L,
                existingSavings.getMonthYear()
        );
        assertNotNull(savingsDTO);
    }

    @Test
    public void testSaveNewSavings() {
        SavingConfigurations savingConfigurations = new SavingConfigurations();
        savingConfigurations.setUserId("1");
        savingConfigurations.setAmount(String.valueOf(1000));

        User user = User.builder()
                .userId(1L)
                .build();

        Long userSavings = 100L;

        when(userRepository.findById(Long.valueOf(savingConfigurations.getUserId()))).thenReturn(Optional.of(user));
        when(savingRepository.findUserTotalSavings(user)).thenReturn(userSavings);

        SavingsDTO savingsDTO = financeService.saveSavingSetting(savingConfigurations);
        verify(userRepository, times(1)).findById(1L);
        verify(savingRepository).findUserTotalSavings(user);
        assertNotNull(savingsDTO);
    }
}
