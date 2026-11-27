
package com.transformatives.backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.transformatives.backend.dto.ShipmentResponse;
import com.transformatives.backend.entity.Shipment;
import com.transformatives.backend.entity.ShipmentStatusHistory;
import com.transformatives.backend.repository.ShipmentRepository;
import com.transformatives.backend.repository.ShipmentStatusHistoryRepository;

@Service
public class CourierShipmentService {

    private final ShipmentRepository shipments;
    private final ShipmentStatusHistoryRepository history;

    public CourierShipmentService(
            ShipmentRepository shipments,
            ShipmentStatusHistoryRepository history) {

        this.shipments = shipments;
        this.history = history;
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> myShipments(
            String courierUsername) {

        return shipments
                .findByAssignedCourierOrderByIdDesc(courierUsername)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ShipmentResponse updateStatus(
            String trackingNumber,
            String requestedStatus,
            String courierUsername) {

        final String expectedStatus;

        if ("IN_TRANSIT".equals(requestedStatus)) {
            expectedStatus = "PENDING";
        } else if ("DELIVERED".equals(requestedStatus)) {
            expectedStatus = "IN_TRANSIT";
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported status"
            );
        }

        int changed = shipments.updateStatusForCourier(
                trackingNumber,
                courierUsername,
                expectedStatus,
                requestedStatus
        );

        if (changed == 0) {
            // Do not disclose whether another courier owns it.
            if (shipments.findByTrackingNumberAndAssignedCourier(
                    trackingNumber,
                    courierUsername
            ).isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found"
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invalid shipment status transition"
            );
        }

        if (changed != 1) {
            throw new IllegalStateException(
                    "Unexpected status update count"
            );
        }

        history.save(new ShipmentStatusHistory(
                trackingNumber,
                expectedStatus,
                requestedStatus,
                courierUsername
        ));

        Shipment updated = shipments
                .findByTrackingNumberAndAssignedCourier(
                        trackingNumber,
                        courierUsername
                )
                .orElseThrow(() -> new IllegalStateException(
                        "Updated shipment disappeared"
                ));

        return toResponse(updated);
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
                shipment.getPackageType(),
                shipment.getAssignedCourier()
        );
    }
}
