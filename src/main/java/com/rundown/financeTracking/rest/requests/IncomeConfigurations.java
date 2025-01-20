package com.rundown.financeTracking.rest.requests;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public final class IncomeConfigurations {
    private String username;
    private String sourceName;
    private String type;
    private String amount;
    private String description;
    private String incomeDate;
}
