
package com.transformatives.backend.dto;

public record RegisterResponse(
    Long id,
    String fullName,
    String username,
    String email,
    String role
) {}
