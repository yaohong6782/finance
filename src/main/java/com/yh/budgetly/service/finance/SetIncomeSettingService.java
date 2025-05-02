package com.yh.budgetly.service.finance;

import com.yh.budgetly.constants.CommonVariables;
import com.yh.budgetly.entity.Income;
import com.yh.budgetly.entity.User;
import com.yh.budgetly.exceptions.CustomException;
import com.yh.budgetly.interfaces.ServiceHandler;
import com.yh.budgetly.kafka.IncomeProducer;
import com.yh.budgetly.mapper.IncomeMapper;
import com.yh.budgetly.mapper.SavingsMapper;
import com.yh.budgetly.mapper.UserMapper;
import com.yh.budgetly.repository.IncomeRepository;
import com.yh.budgetly.repository.SavingRepository;
import com.yh.budgetly.repository.UserRepository;
import com.yh.budgetly.rest.dtos.IncomeDTO;
import com.yh.budgetly.rest.dtos.UserDTO;
import com.yh.budgetly.rest.requests.IncomeConfigurations;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@Qualifier("setIncomeSettingService")
public class SetIncomeSettingService implements ServiceHandler<IncomeDTO, IncomeConfigurations> {

    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;
    private final SavingRepository savingRepository;
    private final IncomeMapper incomeMapper;
    private final SavingsMapper savingsMapper;
    private final UserMapper userMapper;
    private final IncomeProducer incomeProducer;


    @Override
    public IncomeDTO save(IncomeConfigurations incomeConfigurations) {
        log.info("Save Income setting initialised");
        if (incomeConfigurations.getIncomeDate() == null || incomeConfigurations.getIncomeDate().isBlank()) {
            ZonedDateTime currentDateTime = ZonedDateTime.now();
            LocalDate incomeDate = currentDateTime.withDayOfMonth(25).toLocalDate();
            incomeConfigurations.setIncomeDate(incomeDate.toString());
        }
        Long userId = Long.valueOf(incomeConfigurations.getUserId());
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(CommonVariables.USER_NOT_FOUND, HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase()));

        UserDTO userDTO = userMapper.mapUserToUserDTO(user);

        IncomeDTO incomeDTO = IncomeDTO.builder()
                .userDTO(userDTO)
                .sourceName(incomeConfigurations.getSource())
                .amount((incomeConfigurations.getAmount() == null || incomeConfigurations.getAmount().trim().isEmpty()) ?
                        BigDecimal.ZERO : new BigDecimal(incomeConfigurations.getAmount()))
                .description(incomeConfigurations.getDescription())
                .createdAt(LocalDateTime.now())
                .incomeDate(checkIfDateIsNull(incomeConfigurations.getIncomeDate()))
                .updatedAt(LocalDate.now())
                .build();

        LocalDate currentDate = LocalDate.of(2025, 04, 1); // December 1st, 2023
        Income saveIncome = incomeMapper.incomeDTOtoIncome(incomeDTO);
        if (checkIfCorporateJobExist(String.valueOf(userId), currentDate)) {
            log.info("there exist a corporate job this month, recommend update instead");
        } else {
            saveIncome.setIncomeDate(currentDate);
            incomeRepository.save(saveIncome);
        }
        return incomeDTO;
    }

    //    @Scheduled(cron = "0 0 0 25 * ?")
//    @Scheduled(cron = "*/3 * * * * ?")
    public void monthlyIncomeScheudler() {
        log.info("Monthly income scheduler triggered");
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOList = userMapper.mapUserListToUserDTOList(users);
        incomeProducer.sendIncomeEvent(userDTOList);
    }

    public void handleAutomatedIncomeForUser(UserDTO user) {
        log.info("Hello consumer has called me to settle : {} ", user);
        List<Income> defaultIncome = incomeRepository.findAllBySourceName("Corporate Job");
        BigDecimal defaultIncomeAmount = defaultIncome.isEmpty() ? BigDecimal.ZERO : defaultIncome.getFirst().getAmount();
//
        LocalDate currentDate = LocalDate.of(2025, 8, 25); // December 1st, 2023
////        LocalDate currentDate = LocalDate.now();
        if (checkIfCorporateJobExist(String.valueOf(user.getUserId()), currentDate)) {
            log.info("Corporate job already exists for user {}, skipping", user.getUserId());
            return;
        }

        IncomeDTO incomeDTO = IncomeDTO.builder()
                .userDTO(user)
                .sourceName("Corporate Job")
                .amount(defaultIncomeAmount)
                .description("Automated Monthly Entry")
                .createdAt(LocalDateTime.now())
                .incomeDate(currentDate)
                .updatedAt(currentDate)
                .build();

        try {
            incomeRepository.save(incomeMapper.incomeDTOtoIncome(incomeDTO));
        } catch (Exception e) {
            log.error("Something went wrong saving monthly income");
            throw e;
        }
        log.info("Auto income saved for user {} with amount {}", user.getUserId(), defaultIncomeAmount);
    }

    private LocalDate checkIfDateIsNull(String date) {
        return date == null ? null : LocalDate.parse(date);
    }

    private boolean checkIfCorporateJobExist(String userId, LocalDate currentDate) {
        int simulatedMonth = currentDate.getMonthValue(); // 12
        int simulatedYear = currentDate.getYear();

        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        return incomeRepository.countCorporateJobForMonthAndYear(String.valueOf(userId), simulatedMonth, simulatedYear) != 0;
    }
}
