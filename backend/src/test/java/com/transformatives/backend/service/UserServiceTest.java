
package com.transformatives.backend.service;

import com.transformatives.backend.dto.RegisterRequest;
import com.transformatives.backend.dto.RegisterResponse;
import com.transformatives.backend.entity.User;
import com.transformatives.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    private RegisterRequest validRequest() {
        return new RegisterRequest(
                "Test Customer",
                "testcustomer",
                "test@example.com",
                "SecurePassword123!"
        );
    }

    @Test
    void registrationShouldHashPassword() {
        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(validRequest());

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());

        String storedHash = captor.getValue().getPasswordHash();

        assertNotEquals("SecurePassword123!", storedHash);
        assertTrue(new BCryptPasswordEncoder()
                .matches("SecurePassword123!", storedHash));
    }

    @Test
    void registrationShouldAssignCustomerRole() {
        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponse response = userService.register(validRequest());

        assertEquals("CUSTOMER", response.role());
        assertEquals("testcustomer", response.username());
    }

    @Test
    void duplicateUsernameShouldBeRejected() {
        when(userRepository.existsByUsername("testcustomer"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.register(validRequest())
        );

        assertEquals(409, exception.getStatusCode().value());
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }
}
