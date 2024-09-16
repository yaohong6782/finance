package com.rundown.budgetTracking.mapper;

import com.rundown.budgetTracking.entity.Income;
import com.rundown.budgetTracking.rest.dtos.IncomeDTO;
import com.rundown.budgetTracking.rest.requests.IncomeRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IncomeMapper {

    IncomeDTO toIncomeDTO(IncomeRequest incomeRequest);
    Income IncomeDTOtoIncome(IncomeDTO incomeDTO);

}
