package com.rundown.financeTracking.rest.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IncomeDTO {
    private UserDTO userDTO;
    private String sourceName;
    private Float amount;
    private String description;
    private LocalDate incomeDate;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
