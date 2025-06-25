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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Objects;

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
        log.info("Save Income setting initialised values are : {} ", incomeConfigurations);
        if (incomeConfigurations.getIncomeDate() == null || incomeConfigurations.getIncomeDate().isBlank()) {
            incomeConfigurations.setIncomeDate("");
        }
        Long userId = Long.valueOf(incomeConfigurations.getUserId());
        log.info("user id : {} " , userId);
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
                .incomeDate(checkIfDateIsNull(incomeConfigurations.getIncomeDate(), incomeConfigurations.getSource(), incomeConfigurations.getIncomeDate()))
                .updatedAt(LocalDate.now())
                .recurring(false)
                .build();

        LocalDate currentIncomeDate = LocalDate.now();
        Income saveIncome = incomeMapper.incomeDTOtoIncome(incomeDTO);
        log.info("income dto date : {}, {}  " , incomeDTO.getIncomeDate(), currentIncomeDate);
        if (checkIfCorporateJobExist(String.valueOf(userId), currentIncomeDate, "Corporate Job")) {
            int currentMonth = currentIncomeDate.getMonthValue();
            int currentYear = currentIncomeDate.getYear();
            Income existedIncome = incomeRepository.findBySourceNameAndMonthYear(String.valueOf(userId), incomeConfigurations.getSource(), currentMonth, currentYear);
            log.info("existed income ready to be updated : {} ", existedIncome);
        } else {
            saveIncome.setIncomeDate(incomeDTO.getIncomeDate());
            incomeRepository.save(saveIncome);
            log.info("SAVED SUCCESSFUL : {} ", incomeDTO);
        }
        return incomeDTO;
    }

    //    @Scheduled(cron = "0 0 0 25 * ?")
//    @Scheduled(cron = "*/3 * * * * ?")
    public void monthlyIncomeScheduler() {
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
        LocalDate currentDate = LocalDate.now();
        if (checkIfCorporateJobExist(String.valueOf(user.getUserId()), currentDate, "Corporate Job")) {
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

    private LocalDate checkIfDateIsNull(String date, String incomeSource, String selectedDate) {
        if (date == null || date.isBlank()) {
            if ("Corporate Job".equalsIgnoreCase(incomeSource)) {
                log.info("Corporate job with no date");
                return OffsetDateTime.now().withDayOfMonth(25).toLocalDate();
            }
            log.info("date is empty, setting to local date now");
            return LocalDate.now();
        }
        return OffsetDateTime.parse(selectedDate).toLocalDate();
    }

    private boolean checkIfCorporateJobExist(String userId, LocalDate currentDate, String source) {
        int currentMonth = currentDate.getMonthValue();
        int currentYear = currentDate.getYear();
        return incomeRepository.countCorporateJobForMonthAndYear(String.valueOf(userId), source, currentMonth, currentYear) != 0;
    }
}
