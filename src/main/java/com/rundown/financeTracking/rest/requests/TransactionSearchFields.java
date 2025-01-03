package com.rundown.financeTracking.rest.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionSearchFields {
    private String username;
    private SearchFields searchFields;
}
