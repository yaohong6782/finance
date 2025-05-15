package com.yh.budgetly.kafka;

import com.yh.budgetly.rest.dtos.UserDTO;
import com.yh.budgetly.service.finance.SetIncomeSettingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class IncomeConsumer {
    private final SetIncomeSettingService saveIncomeSettingService;

//    @KafkaListener(topics = "income-topic", groupId = "income-group", containerFactory = "userKafkaListenerContainerFactory")
    public void consumeIncomeEvent(UserDTOList wrapper) {
        List<UserDTO> userList = wrapper.getUserDTOs();
        log.info("Consumer is working on income for {} users", userList.size());
        for (UserDTO user : userList) {
            saveIncomeSettingService.handleAutomatedIncomeForUser(user);
        }
    }

//    @KafkaListener(topics = "income-topic", groupId = "income-group", containerFactory = "userKafkaListenerContainerFactory")
//    public void consumeIncomeEvent(UserDTO user) {
//        log.info("Consumer is working on income for user : {} ", user.getUsername());
//
//        // I want to pass the userDTO to this function below
//        saveIncomeSettingService.handleAutomatedIncomeForUser(user);
//    }



}
