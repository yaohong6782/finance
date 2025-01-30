package com.rundown.financeTracking.rest.requests;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public final class IncomeConfigurations {
    private String userId;
    private String source;
    private String amount;
    private String description;
    private String incomeDate;
    private String updatedAt;
}