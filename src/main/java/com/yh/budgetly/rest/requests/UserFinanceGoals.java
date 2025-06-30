package com.yh.budgetly.rest.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class UserFinanceGoals extends MandatoryFields {
     private String expectedExpenses;
     private String savingGoals;
     private String monthYear;
}
