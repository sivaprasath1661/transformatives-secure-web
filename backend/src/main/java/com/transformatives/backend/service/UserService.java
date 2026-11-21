
package com.transformatives.backend.service;

import com.transformatives.backend.dto.RegisterRequest;
import com.transformatives.backend.dto.RegisterResponse;
import com.transformatives.backend.entity.User;
import com.transformatives.backend.repository.UserRepository;
import com.transformatives.backend.dto.LoginRequest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder(12);

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String fullName = request.fullName().trim();
        String username = request.username().trim();
        String email = request.email().trim()
                .toLowerCase(Locale.ROOT);

        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must not exceed 72 bytes"
            );
        }

        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username is already registered"
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        String passwordHash =
                passwordEncoder.encode(request.password());

        User user = new User(
                fullName,
                username,
                email,
                passwordHash,
                "CUSTOMER"
        );

        try {
            User savedUser = userRepository.saveAndFlush(user);

            return new RegisterResponse(
                    savedUser.getId(),
                    savedUser.getFullName(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getRole()
            );

        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username or email is already registered"
            );
        }
    }
    // Verify login credentials against the stored BCrypt hash
    public boolean verifyCredentials(LoginRequest request) {

        return userRepository.findByUsername(request.username().trim())
                .map(user -> passwordEncoder.matches(
                        request.password(),
                        user.getPasswordHash()
                ))
                .orElse(false);
    }
}
