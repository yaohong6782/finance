package com.rundown.financeTracking.mapper;

import com.rundown.financeTracking.entity.Income;
import com.rundown.financeTracking.rest.dtos.IncomeDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IncomeMapper {

    Income IncomeDTOtoIncome(IncomeDTO incomeDTO);

}
