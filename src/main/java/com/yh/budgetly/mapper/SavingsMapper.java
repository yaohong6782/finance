package com.yh.budgetly.mapper;

import com.yh.budgetly.entity.Savings;
import com.yh.budgetly.rest.dtos.SavingsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SavingsMapper {

    @Mapping(target="userDTO", ignore = true)
    SavingsDTO savingsToSavingsDTO(Savings savings);

    @Mapping(target="userDTO", ignore = true)
    List<SavingsDTO> incomeListToIncomeDTOList(List<Savings> savings);

    @Mapping(source="userDTO", target="user")
    @Mapping(source="savingsAmount", target="savingsAmount")
    Savings savingsDTOToSavings(SavingsDTO savingsDTO);

}
