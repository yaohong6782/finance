package com.rundown.financeTracking.rest.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SavingsDTO {
    @JsonIgnore
    private UserDTO userDTO;

    private LocalDate monthYear;
    private String totalIncome;
    private String totalExpenses;
    private String savings;
    private LocalDate createdAt;
}
