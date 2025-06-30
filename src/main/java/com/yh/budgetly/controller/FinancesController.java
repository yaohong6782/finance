package com.yh.budgetly.controller;

import com.yh.budgetly.exceptions.ErrorResponse;
import com.yh.budgetly.interfaces.ServiceHandler;
import com.yh.budgetly.rest.dtos.IncomeDTO;
import com.yh.budgetly.rest.dtos.SavingsDTO;
import com.yh.budgetly.rest.dtos.UserDTO;
import com.yh.budgetly.rest.requests.*;
import com.yh.budgetly.rest.responses.finances.FinanceSetting;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/finances")
public class FinancesController {

    @Qualifier("setIncomeSettingService")
    private final ServiceHandler<IncomeDTO, IncomeConfigurations> setIncomeSettingService;

    @Qualifier("retrieveFinances")
    private final ServiceHandler<FinanceSetting, UserDTO> retrieveFinances;

    @Qualifier("retrieveFilteredIncome")
    private final ServiceHandler<List<IncomeDTO>, UserIncomeDetailsDTO> retrieveFilteredIncome;

    @Qualifier("setFinanceGoals")
    private final ServiceHandler<SavingsDTO, UserFinanceGoals> setFinanceGoals;

    @Qualifier("viewFinanceGoals")
    private final ServiceHandler<SavingsDTO, UserFinanceGoals> viewFinanceGoals;

    @Tag(name = "Finances", description = "This API configures user's income settings")
    @PostMapping("/set-income")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = IncomeDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Unable to find requested endpoint",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<IncomeDTO> setIncome(@RequestBody IncomeConfigurations incomeConfigurations) {

        log.info("Income configurations : {} ", incomeConfigurations);
        IncomeDTO incomeDTO = setIncomeSettingService.save(incomeConfigurations);
        return new ResponseEntity<>(incomeDTO, HttpStatus.OK);
    }

    @Tag(name = "Finances", description = "This API retrieves finance settings")
    @PostMapping("/retrieve-setting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FinanceSetting.class))),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Unable to find requested endpoint",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FinanceSetting> retrieveAllSettings(@RequestBody UserDTO userDTO) {

        log.info("Users configurations : {} ", userDTO);
        FinanceSetting financeSetting = retrieveFinances.retrieve(userDTO);

        return new ResponseEntity<>(financeSetting, HttpStatus.OK);
    }

    @Tag(name = "Finances", description = "This API retrieves finance settings")
    @PostMapping("/retrieve-filtered-income")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = IncomeDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Unable to find requested endpoint",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<IncomeDTO>> retrieveFilteredIncome(@RequestBody UserIncomeDetailsDTO userIncomeDetailsDTO) {

        log.info("Request to retrieve filtered income : {} ", userIncomeDetailsDTO);
        List<IncomeDTO> incomeDTO = retrieveFilteredIncome.retrieve(userIncomeDetailsDTO);

        return new ResponseEntity<>(incomeDTO, HttpStatus.OK);
    }

    @Tag(name = "Finances", description = "This API retrieves finance settings")
    @PostMapping("/finance-goals")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserFinanceGoals.class))),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Unable to find requested endpoint",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SavingsDTO> setFinanceGoals(@RequestBody UserFinanceGoals userFinanceGoals) {

        log.info("Finance goal initialised : {} ", userFinanceGoals);
        SavingsDTO savingsDTO = setFinanceGoals.save(userFinanceGoals);

        return new ResponseEntity<>(savingsDTO, HttpStatus.OK);
    }
    @Tag(name = "Finances", description = "This API retrieves finance settings")
    @PostMapping("/view-finance-goals")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserFinanceGoals.class))),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Unable to find requested endpoint",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SavingsDTO> viewFinanceGoals(@RequestBody UserFinanceGoals userFinanceGoals) {

        log.info("Viewing Finance goal initialised : {} ", userFinanceGoals);
        SavingsDTO savingsDTO = viewFinanceGoals.retrieve(userFinanceGoals);

        return new ResponseEntity<>(savingsDTO, HttpStatus.OK);
    }
}
