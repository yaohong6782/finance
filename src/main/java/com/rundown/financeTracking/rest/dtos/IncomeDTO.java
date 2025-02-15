package com.rundown.financeTracking.rest.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IncomeDTO {
//    @JsonIgnore
    private UserDTO userDTO;
    private String sourceName;
    private BigDecimal amount;
    private String description;
    private LocalDate incomeDate;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private Boolean recurring;
}
