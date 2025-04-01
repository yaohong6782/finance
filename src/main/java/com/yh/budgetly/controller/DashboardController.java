package com.yh.budgetly.controller;


import com.yh.budgetly.exceptions.CustomException;
import com.yh.budgetly.rest.dtos.TransactionDTO;
import com.yh.budgetly.rest.requests.MandatoryFields;
import com.yh.budgetly.rest.requests.TransactionRequestFields;
import com.yh.budgetly.rest.responses.dashboard.DashboardResponse;
import com.yh.budgetly.service.DashboardService;
import com.yh.budgetly.service.FinanceService;
import com.yh.budgetly.service.TransactionService;
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
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @Tag(name = "Dashboard", description = "This API displays the entire dashboard information")
    @PostMapping("/view")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", description = "Unable to find requested endpoint"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<?> dashboardInformation(@RequestBody MandatoryFields request) {

        DashboardResponse response = dashboardService.dashboardResponse(request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
