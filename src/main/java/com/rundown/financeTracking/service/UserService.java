package com.rundown.financeTracking.service;

import com.rundown.financeTracking.config.JwtService;
import com.rundown.financeTracking.entity.User;
import com.rundown.financeTracking.exceptions.CustomException;
import com.rundown.financeTracking.repository.UserRepository;
import com.rundown.financeTracking.rest.dtos.UserDTO;
import com.rundown.financeTracking.rest.requests.LoginRequest;
import com.rundown.financeTracking.rest.responses.UserLoginJwt;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private PasswordEncoder passwordEncoder;

    public UserDTO userSignUp(UserDTO userRequest) {
        Optional<User> existingUser = userRepository.findByUsernameOrEmail(userRequest.getUsername(), userRequest.getEmail());
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (user.getUsername().equalsIgnoreCase(userRequest.getUsername())) {
                throw new CustomException("Username is already taken", HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
            } else if (user.getEmail().equalsIgnoreCase(userRequest.getEmail())) {
                throw new CustomException("Email is already taken", HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
            }
        }
        User saveUser = User.builder()
                .username(userRequest.getUsername())
                .email(userRequest.getEmail())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();

        User createUser = userRepository.save(saveUser);
        return UserDTO.builder()
                .username(createUser.getUsername())
                .email(createUser.getEmail())
                .updatedAt(LocalDate.now())
                .createdAt(LocalDate.now())
                .build();

    }

    public UserLoginJwt userLoginJwt(LoginRequest loginRequest) throws CustomException {
        log.info("User login service : {}", loginRequest);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );


        var userExist = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() ->
                        new CustomException("User does not exist", HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase()));
        log.info("this is the user we found : {}", userExist);

        String jwtToken = jwtService.generateToken(userExist);
        return UserLoginJwt.builder().accessToken(jwtToken).build();
    }
}
