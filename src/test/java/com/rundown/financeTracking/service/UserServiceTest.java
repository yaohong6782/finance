package com.rundown.financeTracking.service;

import com.rundown.financeTracking.config.JwtService;
import com.rundown.financeTracking.entity.User;
import com.rundown.financeTracking.exceptions.CustomException;
import com.rundown.financeTracking.repository.UserRepository;
import com.rundown.financeTracking.rest.dtos.UserDTO;
import com.rundown.financeTracking.rest.requests.LoginRequest;
import com.rundown.financeTracking.rest.responses.UserLoginJwt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private LoginRequest loginRequest;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private User user;

    @InjectMocks
    private UserService userService;

    @Test
    public void testUserSignUp() {
        UserDTO userRequest = UserDTO.builder()
                .username("testUser")
                .email("test@email.com")
                .password("123")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        userService.userSignUp(userRequest);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testUserSignUp_userNameExist() {
        UserDTO userRequest = UserDTO.builder()
                .username("testUser")
                .email("test@email.com")
                .password("123")
                .build();

        when(user.getUsername()).thenReturn("testUser");
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(user));

        CustomException thrownException = assertThrows(CustomException.class, () -> {
            userService.userSignUp(userRequest);
        });

        assertEquals("Username is already taken", thrownException.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testUserSignUp_userEmailExist() {
        UserDTO userRequest = UserDTO.builder()
                .username("testUser")
                .email("test@email.com")
                .password("123")
                .build();

        when(user.getUsername()).thenReturn("testUser1");
        when(user.getEmail()).thenReturn("test@email.com");
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(user));

        CustomException thrownException = assertThrows(CustomException.class, () -> {
            userService.userSignUp(userRequest);
        });

        assertEquals("Email is already taken", thrownException.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

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
