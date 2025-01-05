package com.rundown.financeTracking.rest.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public sealed class SearchMandatoryFields permits TransactionSearchFields{
    private String username;
}
