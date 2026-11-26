
package com.transformatives.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transformatives.backend.dto.ShipmentRequest;
import com.transformatives.backend.dto.ShipmentResponse;
import com.transformatives.backend.service.ShipmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customer/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    public ResponseEntity<ShipmentResponse> create(
            @Valid @RequestBody ShipmentRequest request,
            Authentication authentication) {

        ShipmentResponse shipment = shipmentService.create(
                request,
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(shipment);
    }

    @GetMapping
    public List<ShipmentResponse> myShipments(
            Authentication authentication) {

        return shipmentService.findMyShipments(
                authentication.getName()
        );
    }

    @GetMapping("/{trackingNumber}")
    public ShipmentResponse track(
            @PathVariable String trackingNumber,
            Authentication authentication) {

        return shipmentService.track(
                trackingNumber,
                authentication.getName()
        );
    }

    @PatchMapping("/{trackingNumber}/cancel")
    public ShipmentResponse cancel(
            @PathVariable String trackingNumber,
            Authentication authentication) {

        return shipmentService.cancel(
                trackingNumber,
                authentication.getName()
        );
    }
}
