
package com.transformatives.backend.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @GetMapping("/dashboard")
    public Map<String, String> dashboard(
            Authentication authentication) {

        return Map.of(
                "message", "Customer dashboard access granted",
                "username", authentication.getName(),
                "role", "CUSTOMER"
        );
    }
}
