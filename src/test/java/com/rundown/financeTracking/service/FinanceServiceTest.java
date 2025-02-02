package com.rundown.financeTracking.service;

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
import com.rundown.financeTracking.rest.responses.finances.FinanceSetting;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

}
