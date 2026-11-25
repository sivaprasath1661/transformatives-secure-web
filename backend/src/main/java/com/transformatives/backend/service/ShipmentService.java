
package com.transformatives.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.transformatives.backend.dto.ShipmentRequest;
import com.transformatives.backend.dto.ShipmentResponse;
import com.transformatives.backend.entity.Shipment;
import com.transformatives.backend.repository.ShipmentRepository;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;

    public ShipmentService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Transactional
    public ShipmentResponse create(
            ShipmentRequest request,
            String username) {

        String trackingNumber = "TRF-" +
                UUID.randomUUID().toString()
                        .replace("-", "")
                        .substring(0, 16)
                        .toUpperCase();

        Shipment shipment = new Shipment(
                trackingNumber,
                username,
                request.recipientName().trim(),
                request.destinationCity().trim(),
                request.deliveryAddress().trim()
        );

        return toResponse(shipmentRepository.save(shipment));
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> findMyShipments(
            String username) {

        return shipmentRepository
                .findByOwnerUsernameOrderByIdDesc(username)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShipmentResponse track(
            String trackingNumber,
            String username) {

        Shipment shipment = shipmentRepository
                .findByTrackingNumberAndOwnerUsername(
                        trackingNumber, username
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found"
                ));

        return toResponse(shipment);
    }

    private ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getTrackingNumber(),
                shipment.getRecipientName(),
                shipment.getDestinationCity(),
                shipment.getDeliveryAddress(),
                shipment.getStatus()
        );
    }
}
