package com.rundown.financeTracking.controller;

import com.rundown.financeTracking.exceptions.CustomException;
import com.rundown.financeTracking.rest.dtos.IncomeDTO;
import com.rundown.financeTracking.rest.requests.IncomeConfigurations;
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

import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/finances")
public class FinancesController {
    private final FinanceService financeService;

    @Tag(name = "Setting income configurations", description = "This API configures user's income settings")
    @PostMapping("/setIncome")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", description = "Unable to find requested endpoint"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<IncomeDTO> setIncome(@RequestBody IncomeConfigurations incomeConfigurations) throws CustomException {

        log.info("Income configurations : {} ", incomeConfigurations);
        IncomeDTO incomeDTO = financeService.saveIncomeSettings(incomeConfigurations);
        return new ResponseEntity<>(incomeDTO, HttpStatus.NO_CONTENT);
    }
}
