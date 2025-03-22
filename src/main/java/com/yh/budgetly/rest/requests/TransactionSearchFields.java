package com.yh.budgetly.rest.requests;

import lombok.*;
import lombok.experimental.SuperBuilder;


@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public final class TransactionSearchFields extends MandatoryFields {
    private SearchFields searchFields;
}
