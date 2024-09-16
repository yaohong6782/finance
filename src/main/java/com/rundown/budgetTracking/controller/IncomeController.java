package com.rundown.budgetTracking.controller;

import com.rundown.budgetTracking.exceptions.CustomException;
import com.rundown.budgetTracking.rest.dtos.IncomeDTO;
import com.rundown.budgetTracking.rest.requests.IncomeRequest;
import com.rundown.budgetTracking.service.IncomeService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/income")
public class IncomeController {

    private IncomeService incomeService;

    @PostMapping("/salarySource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", description = "Unable to find requested endpoint"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<IncomeDTO> salarySource(@RequestBody IncomeRequest incomeRequest) throws CustomException {
        log.info("Income request for salary source");

        IncomeDTO incomeDTO = incomeService.userSalaryIncome(incomeRequest);

        return new ResponseEntity<>(incomeDTO, HttpStatus.CREATED);
    }
}
