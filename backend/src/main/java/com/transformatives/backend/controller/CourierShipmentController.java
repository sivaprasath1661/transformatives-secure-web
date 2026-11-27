
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
import com.transformatives.backend.dto.ShipmentStatusUpdateRequest;
import com.transformatives.backend.service.CourierShipmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/courier")
public class CourierShipmentController {

    private final CourierShipmentService service;

    public CourierShipmentController(
            CourierShipmentService service) {
        this.service = service;
    }

    @GetMapping("/access")
    public Map<String, String> access(Authentication auth) {
        return Map.of(
                "role", "COURIER",
                "username", auth.getName()
        );
    }

    @GetMapping("/shipments")
    public List<ShipmentResponse> myShipments(
            Authentication auth) {

        return service.myShipments(auth.getName());
    }

    @PatchMapping("/shipments/{trackingNumber}/status")
    public ShipmentResponse updateStatus(
            @PathVariable String trackingNumber,
            @Valid @RequestBody ShipmentStatusUpdateRequest request,
            Authentication auth) {

        return service.updateStatus(
                trackingNumber,
                request.status(),
                auth.getName()
        );
    }
}
