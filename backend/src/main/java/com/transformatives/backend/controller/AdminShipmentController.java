
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

import com.transformatives.backend.dto.CourierAssignmentRequest;
import com.transformatives.backend.dto.ShipmentResponse;
import com.transformatives.backend.dto.ShipmentStatusHistoryResponse;
import com.transformatives.backend.dto.ShipmentStatusUpdateRequest;
import com.transformatives.backend.entity.ShipmentAssignmentHistory;
import com.transformatives.backend.service.AdminShipmentService;
import com.transformatives.backend.service.CourierAssignmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminShipmentController {

    private final AdminShipmentService adminShipmentService;
    private final CourierAssignmentService assignmentService;

    public AdminShipmentController(
            AdminShipmentService adminShipmentService,
            CourierAssignmentService assignmentService) {

        this.adminShipmentService = adminShipmentService;
        this.assignmentService = assignmentService;
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

    @PatchMapping("/shipments/{trackingNumber}/assign")
    public ShipmentResponse assign(
            @PathVariable String trackingNumber,
            @Valid @RequestBody CourierAssignmentRequest request,
            Authentication authentication) {

        return assignmentService.assign(
                trackingNumber,
                request.courierUsername(),
                authentication.getName()
        );
    }

    @GetMapping("/shipments/{trackingNumber}/history")
    public List<ShipmentStatusHistoryResponse> history(
            @PathVariable String trackingNumber) {

        return adminShipmentService.getHistory(trackingNumber);
    }

    @GetMapping("/shipments/{trackingNumber}/assignments")
    public List<ShipmentAssignmentHistory> assignments(
            @PathVariable String trackingNumber) {

        return assignmentService.history(trackingNumber);
    }
}
