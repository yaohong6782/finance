package com.rundown.financeTracking.rest.requests;

import lombok.*;
import lombok.experimental.SuperBuilder;


@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public final class TransactionSearchFields extends SearchMandatoryFields {
    private SearchFields searchFields;
}
