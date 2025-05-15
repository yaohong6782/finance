package com.yh.budgetly.controller;

import com.yh.budgetly.exceptions.CustomException;
import com.yh.budgetly.exceptions.ErrorResponse;
import com.yh.budgetly.rest.dtos.UserDTO;
import com.yh.budgetly.rest.requests.LoginRequest;
import com.yh.budgetly.rest.responses.UserLoginJwt;
import com.yh.budgetly.rest.responses.dashboard.DashboardResponse;
import com.yh.budgetly.service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Tag(name = "User Controller", description = "This API handles user login")
    @PostMapping("/login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No such user found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserLoginJwt> userLogin(@RequestBody LoginRequest loginRequest) throws CustomException {
        log.info("User Login");
        UserLoginJwt userLoginJwt = userService.userLoginJwt(loginRequest);
        return new ResponseEntity<>(userLoginJwt, HttpStatus.OK);
    }

    @Tag(name = "User Controller", description = "This API handles user sign up")
    @PostMapping("/signUp")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Unable to create user",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserDTO> userSignUp(@RequestBody UserDTO userRequest) throws CustomException {
        log.info("User sign up {} ", userRequest);
        UserDTO userDTO = userService.userSignUp(userRequest);

        return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
    }
}
