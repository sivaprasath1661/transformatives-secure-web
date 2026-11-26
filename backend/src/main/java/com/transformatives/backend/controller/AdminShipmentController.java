
package com.transformatives.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transformatives.backend.dto.ShipmentResponse;
import com.transformatives.backend.dto.ShipmentStatusHistoryResponse;
import com.transformatives.backend.dto.ShipmentStatusUpdateRequest;
import com.transformatives.backend.service.AdminShipmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminShipmentController {

    private final AdminShipmentService adminShipmentService;

    public AdminShipmentController(
            AdminShipmentService adminShipmentService) {

        this.adminShipmentService = adminShipmentService;
    }

    @GetMapping("/access")
    public Map<String, String> checkAdminAccess() {
        return Map.of("role", "ADMIN");
    }

    @GetMapping("/shipments")
    public List<ShipmentResponse> allShipments() {
        return adminShipmentService.findAllShipments();
    }

    @PatchMapping("/shipments/{trackingNumber}/status")
    public ShipmentResponse updateStatus(
            @PathVariable String trackingNumber,
            @Valid @RequestBody ShipmentStatusUpdateRequest request,
            Authentication authentication) {

        return adminShipmentService.updateStatus(
                trackingNumber,
                request.status(),
                authentication.getName()
        );
    }

    @GetMapping("/shipments/{trackingNumber}/history")
    public List<ShipmentStatusHistoryResponse> history(
            @PathVariable String trackingNumber) {

        return adminShipmentService.getHistory(trackingNumber);
    }
}
