package com.rundown.financeTracking.mapper;

import com.rundown.financeTracking.entity.Income;
import com.rundown.financeTracking.entity.Savings;
import com.rundown.financeTracking.rest.dtos.IncomeDTO;
import com.rundown.financeTracking.rest.dtos.SavingsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SavingsMapper {

    @Mapping(source="user", target="userDTO")
    SavingsDTO savingsToSavingsDTO(Savings savings);

    @Mapping(target="userDTO", ignore = true)
    List<SavingsDTO> incomeListToIncomeDTOList(List<Savings> savings);

    @Mapping(source="userDTO", target="user")
    @Mapping(source="savingsAmount", target="savingsAmount")
    Savings savingsDTOToSavings(SavingsDTO savingsDTO);

}
