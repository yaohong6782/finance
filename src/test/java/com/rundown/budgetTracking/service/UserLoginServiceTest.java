package com.rundown.budgetTracking.service;

import com.rundown.budgetTracking.config.JwtService;
import com.rundown.budgetTracking.entity.User;
import com.rundown.budgetTracking.exceptions.CustomException;
import com.rundown.budgetTracking.repository.UserRepository;
import com.rundown.budgetTracking.rest.requests.LoginRequest;
import com.rundown.budgetTracking.rest.responses.UserLoginJwt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserLoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private LoginRequest loginRequest;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private User user;
    @InjectMocks
    private UserService userService;

    @Test
    public void testUserLoginJwt() throws CustomException {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);

        // Mock the userRepository.findByUsername method to return our test user
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(user));

        // Mock the jwtService.generateToken method to return a dummy token
        String expectedToken = "dummyJwtToken";
        when(jwtService.generateToken(user)).thenReturn(expectedToken);

        // Call the method under test
        UserLoginJwt result = userService.userLoginJwt(loginRequest);

        // Verify the interactions and assert the result
        verify(userRepository, times(1)).findByUsername(loginRequest.getUsername());
        verify(jwtService, times(1)).generateToken(user);

        assertEquals(expectedToken, result.getAccessToken());
    }
}
