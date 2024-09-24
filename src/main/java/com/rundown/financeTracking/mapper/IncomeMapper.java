package com.rundown.financeTracking.mapper;

import com.rundown.financeTracking.entity.Income;
import com.rundown.financeTracking.rest.dtos.IncomeDTO;
import com.rundown.financeTracking.rest.requests.IncomeRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IncomeMapper {

    IncomeDTO toIncomeDTO(IncomeRequest incomeRequest);
    Income IncomeDTOtoIncome(IncomeDTO incomeDTO);

}
