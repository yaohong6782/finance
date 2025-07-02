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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

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
    public ResponseEntity<UserLoginJwt> userLogin(@RequestBody LoginRequest loginRequest, HttpServletResponse response) throws CustomException {
        UserLoginJwt userLoginJwt = userService.userLoginJwt(loginRequest);
        String jwtToken = userLoginJwt.getAccessToken();


        ResponseCookie cookie = ResponseCookie.from("token", jwtToken)
                .httpOnly(true)
                .secure(true)
                .maxAge(Duration.ofHours(24))
                .sameSite("Strict")
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return new ResponseEntity<>(userLoginJwt, HttpStatus.OK);
    }

    @Tag(name = "User Controller", description = "This API handles user login")
    @GetMapping("/logout")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No such user found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) throws CustomException {

        log.info("LOGGED OUT");
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    log.info("Found existing token cookie: " + cookie.getValue());
                }
            }
        }
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(true)
                .maxAge(0)
                .sameSite("Strict")
                .path("/")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok("Logged out successfully");
    }

    @Tag(name = "User Controller", description = "This API handles user sign up")
    @PostMapping("/register")
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
