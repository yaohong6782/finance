package com.rundown.financeTracking.service;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FinanceServiceTest {

    private static final Logger log = LoggerFactory.getLogger(FinanceServiceTest.class);
    @Mock
    private UserRepository userRepository;

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private IncomeMapper incomeMapper;

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
        incomeConfigurations.setUserId(userId.toString());

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            financeService.saveIncomeSettings(incomeConfigurations);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
