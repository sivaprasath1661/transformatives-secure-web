
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
import com.transformatives.backend.entity.ShipmentStatusHistory;
import com.transformatives.backend.repository.ShipmentRepository;
import com.transformatives.backend.repository.ShipmentStatusHistoryRepository;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentStatusHistoryRepository historyRepository;

    public ShipmentService(
            ShipmentRepository shipmentRepository,
            ShipmentStatusHistoryRepository historyRepository) {

        this.shipmentRepository = shipmentRepository;
        this.historyRepository = historyRepository;
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
                request.deliveryAddress().trim(),
                request.recipientPhone().trim(),
                request.postalCode().trim(),
                request.packageType()
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

        return toResponse(
                findOwnedShipment(trackingNumber, username)
        );
    }

    @Transactional
    public ShipmentResponse cancel(
            String trackingNumber,
            String username) {

        // One atomic UPDATE checks ownership and current status.
        int updatedRows = shipmentRepository.cancelPendingForOwner(
                trackingNumber,
                username
        );

        if (updatedRows == 0) {

            // Owner-scoped lookup avoids disclosing other shipments.
            shipmentRepository
                    .findByTrackingNumberAndOwnerUsername(
                            trackingNumber,
                            username
                    )
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Shipment not found"
                    ));

            // Shipment exists but is no longer cancellable.
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending shipments can be cancelled"
            );
        }

        if (updatedRows != 1) {
            throw new IllegalStateException(
                    "Unexpected cancellation update count"
            );
        }

        // Record the cancellation in the existing audit table.
        ShipmentStatusHistory history = new ShipmentStatusHistory(
                trackingNumber,
                "PENDING",
                "CANCELLED",
                username
        );

        historyRepository.save(history);

        // Fetch the updated record for an accurate API response.
        Shipment updatedShipment = findOwnedShipment(
                trackingNumber,
                username
        );

        return toResponse(updatedShipment);
    }

    private Shipment findOwnedShipment(
            String trackingNumber,
            String username) {

        return shipmentRepository
                .findByTrackingNumberAndOwnerUsername(
                        trackingNumber,
                        username
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found"
                ));
    }

    private ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getTrackingNumber(),
                shipment.getRecipientName(),
                shipment.getDestinationCity(),
                shipment.getDeliveryAddress(),
                shipment.getStatus(),
                shipment.getRecipientPhone(),
                shipment.getPostalCode(),
                shipment.getPackageType()
        );
    }
}
