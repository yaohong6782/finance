package com.yh.budgetly.kafka;

import com.yh.budgetly.rest.dtos.UserDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class IncomeProducer {
//    private final KafkaTemplate<String, List<UserDTO>> kafkaTemplate;
    private final KafkaTemplate<String, UserDTOList> kafkaTemplate;

    public void sendIncomeEvent(List<UserDTO> user) {
        kafkaTemplate.send("income-topic", new UserDTOList(user));
        log.info("Income event sent to Kafka for user : {}, message : {} ", user.size(), user);
    }

//    public void sendIncomeEvent(String userId, UserDTO user) {
//        kafkaTemplate.send("income-topic", userId, user);
//        log.info("Income event sent to Kafka for user : {}, message : {} ", userId, user);
//    }

}
