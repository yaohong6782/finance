package com.rundown.financeTracking.mapper;

import com.rundown.financeTracking.entity.Income;
import com.rundown.financeTracking.rest.dtos.IncomeDTO;
import com.rundown.financeTracking.rest.requests.IncomeConfigurations;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface IncomeMapper {

    @Mapping(source="userDTO", target="user")
    Income incomeDTOtoIncome(IncomeDTO incomeDTO);

    @Mapping(target="userDTO", ignore = true)
    @Mapping(target = "userDTO.password" , ignore = true)
    @Mapping(target="userDTO.userId", ignore = true)
    IncomeDTO incomeToIncomeDTO(Income income);

    @Mapping(target="userDTO", ignore = true)
    List<IncomeDTO> incomeListToIncomeDTOList(List<Income> income);

    @Mapping(source = "source", target = "sourceName")
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "incomeDate", target = "incomeDate", qualifiedByName = "mapIncomeDate")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "mapUpdatedDate")
    IncomeDTO incomeConfigurationToIncomeDTO(IncomeConfigurations incomeConfigurations);


    @AfterMapping
    default void setAdditionalFields(@MappingTarget IncomeDTO incomeDTO, IncomeConfigurations incomeConfigurations) {
        incomeDTO.setCreatedAt(LocalDate.from(LocalDateTime.now()));
        // Add any additional mappings or logic here
        // Example: Map a new field in the future
        // incomeDTO.setSomeField(incomeConfigurations.getSomeField());
    }

    @Named("mapIncomeDate")
    default LocalDate mapIncomeDate(String incomeDate) {
        return incomeDate == null ? null : ZonedDateTime.parse(incomeDate).toLocalDate();
    }

    @Named("mapUpdatedDate")
    default LocalDate mapUpdatedDate(String updatedDate) {
        return updatedDate == null ? null : ZonedDateTime.parse(updatedDate).toLocalDate();
    }
}
