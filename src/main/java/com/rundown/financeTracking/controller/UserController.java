package com.rundown.financeTracking.controller;

import com.rundown.financeTracking.exceptions.CustomException;
import com.rundown.financeTracking.rest.dtos.UserDTO;
import com.rundown.financeTracking.rest.requests.LoginRequest;
import com.rundown.financeTracking.rest.responses.UserLoginJwt;
import com.rundown.financeTracking.service.UserService;
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
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", description = "No such user found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<UserLoginJwt> userLogin(@RequestBody LoginRequest loginRequest) throws CustomException {
        log.info("User Login");
        UserLoginJwt userLoginJwt = userService.userLoginJwt(loginRequest);
        return new ResponseEntity<>(userLoginJwt, HttpStatus.OK);
    }

    @Tag(name = "User Controller", description = "This API handles user sign up")
    @PostMapping("/signUp")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", description = "Unable to create user"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<UserDTO> userSignUp(@RequestBody UserDTO userRequest) throws CustomException {
        log.info("User sign up {} ", userRequest);
        UserDTO userDTO = userService.userSignUp(userRequest);

        return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
    }
}
