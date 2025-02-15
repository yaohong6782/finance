package com.rundown.financeTracking.controller;

import com.rundown.financeTracking.rest.dtos.IncomeDTO;
import com.rundown.financeTracking.rest.dtos.SavingsDTO;
import com.rundown.financeTracking.rest.dtos.UserDTO;
import com.rundown.financeTracking.rest.requests.IncomeConfigurations;
import com.rundown.financeTracking.rest.requests.SavingConfigurations;
import com.rundown.financeTracking.rest.responses.finances.FinanceSetting;
import com.rundown.financeTracking.service.FinanceService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/finances")
public class FinancesController {
    private final FinanceService financeService;

    @Tag(name = "Finances", description = "This API configures user's income settings")
    @PostMapping("/setIncome")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", description = "Unable to find requested endpoint"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<IncomeDTO> setIncome(@RequestBody IncomeConfigurations incomeConfigurations) {

        log.info("Income configurations : {} ", incomeConfigurations);
        IncomeDTO incomeDTO = financeService.saveIncomeSettings(incomeConfigurations);
        return new ResponseEntity<>(incomeDTO, HttpStatus.OK);
    }

    @Tag(name = "Finances", description = "This API configures user's saving settings")
    @PostMapping("/setSaving")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", description = "Unable to find requested endpoint"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<SavingsDTO> setSaving(@RequestBody SavingConfigurations savingConfigurations) {

        log.info("Savings configurations : {} ", savingConfigurations);
        SavingsDTO savingsDTO = financeService.saveSavingSetting(savingConfigurations);
        return new ResponseEntity<>(savingsDTO, HttpStatus.OK);
    }

    @Tag(name = "Finances", description = "This API retrieves finance settings")
    @PostMapping("/retrieveSettings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", description = "Unable to find requested endpoint"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<FinanceSetting> retrieveAllSettings(@RequestBody UserDTO userDTO) {

        log.info("Users configurations : {} ", userDTO);
        FinanceSetting financeSetting = financeService.financeSettings(userDTO);

        return new ResponseEntity<>(financeSetting,HttpStatus.OK);
    }
}
