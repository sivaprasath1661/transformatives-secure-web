
package com.transformatives.backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.transformatives.backend.dto.ShipmentResponse;
import com.transformatives.backend.entity.Shipment;
import com.transformatives.backend.entity.ShipmentAssignmentHistory;
import com.transformatives.backend.repository.ShipmentAssignmentHistoryRepository;
import com.transformatives.backend.repository.ShipmentRepository;
import com.transformatives.backend.repository.UserRepository;

@Service
public class CourierAssignmentService {

    private final ShipmentRepository shipments;
    private final UserRepository users;
    private final ShipmentAssignmentHistoryRepository assignments;

    public CourierAssignmentService(
            ShipmentRepository shipments,
            UserRepository users,
            ShipmentAssignmentHistoryRepository assignments) {

        this.shipments = shipments;
        this.users = users;
        this.assignments = assignments;
    }

    @Transactional
    public ShipmentResponse assign(
            String trackingNumber,
            String courierUsername,
            String adminUsername) {

        if (!List.of("jetdeliveries", "jkcouriers")
                .contains(courierUsername)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported courier"
            );
        }

        boolean validCourier = users.findByUsername(courierUsername)
                .filter(user -> "COURIER".equals(user.getRole()))
                .isPresent();

        if (!validCourier) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Courier account does not exist or is not enabled as a courier"
            );
        }

        int changed = shipments.assignIfPendingAndUnassigned(
                trackingNumber,
                courierUsername
        );

        if (changed == 0) {
            if (shipments.findByTrackingNumber(trackingNumber).isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found"
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Shipment must be pending and unassigned"
            );
        }

        if (changed != 1) {
            throw new IllegalStateException(
                    "Unexpected assignment count"
            );
        }

        assignments.save(new ShipmentAssignmentHistory(
                trackingNumber,
                courierUsername,
                adminUsername
        ));

        Shipment shipment = shipments
                .findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new IllegalStateException(
                        "Assigned shipment disappeared"
                ));

        return toResponse(shipment);
    }

    @Transactional(readOnly = true)
    public List<ShipmentAssignmentHistory> history(
            String trackingNumber) {

        if (shipments.findByTrackingNumber(trackingNumber).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Shipment not found"
            );
        }

        return assignments
                .findByTrackingNumberOrderByAssignedAtDescIdDesc(
                        trackingNumber
                );
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
