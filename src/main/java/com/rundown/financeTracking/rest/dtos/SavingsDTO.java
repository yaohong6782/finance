package com.rundown.financeTracking.rest.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavingsDTO {
    private UserDTO userDTO;
    private String monthYear;
    private String totalIncome;
    private String totalExpenses;
    private String savingsAmount;
    private LocalDate createdAt;
    private String savingsGoal;
}
