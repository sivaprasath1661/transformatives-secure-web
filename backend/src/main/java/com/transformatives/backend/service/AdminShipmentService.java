
package com.transformatives.backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.transformatives.backend.dto.ShipmentResponse;
import com.transformatives.backend.dto.ShipmentStatusHistoryResponse;
import com.transformatives.backend.entity.Shipment;
import com.transformatives.backend.entity.ShipmentStatusHistory;
import com.transformatives.backend.repository.ShipmentRepository;
import com.transformatives.backend.repository.ShipmentStatusHistoryRepository;

@Service
public class AdminShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentStatusHistoryRepository historyRepository;

    public AdminShipmentService(
            ShipmentRepository shipmentRepository,
            ShipmentStatusHistoryRepository historyRepository) {

        this.shipmentRepository = shipmentRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> findAllShipments() {
        return shipmentRepository.findAllByOrderByIdDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ShipmentResponse updateStatus(
            String trackingNumber,
            String requestedStatus,
            String adminUsername) {

        final String expectedStatus;

        if ("IN_TRANSIT".equals(requestedStatus)) {
            expectedStatus = "PENDING";
        } else if ("DELIVERED".equals(requestedStatus)) {
            expectedStatus = "IN_TRANSIT";
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported shipment status"
            );
        }

        int changed = shipmentRepository.updateStatusIfCurrent(
                trackingNumber,
                expectedStatus,
                requestedStatus
        );

        if (changed == 0) {
            if (shipmentRepository
                    .findByTrackingNumber(trackingNumber)
                    .isEmpty()) {

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

        ShipmentStatusHistory history = new ShipmentStatusHistory(
                trackingNumber,
                expectedStatus,
                requestedStatus,
                adminUsername
        );

        historyRepository.save(history);

        Shipment updated = shipmentRepository
                .findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found"
                ));

        return toResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<ShipmentStatusHistoryResponse> getHistory(
            String trackingNumber) {

        if (shipmentRepository
                .findByTrackingNumber(trackingNumber)
                .isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Shipment not found"
            );
        }

        return historyRepository
                .findByTrackingNumberOrderByChangedAtDescIdDesc(
                        trackingNumber
                )
                .stream()
                .map(event -> new ShipmentStatusHistoryResponse(
                        event.getId(),
                        event.getTrackingNumber(),
                        event.getOldStatus(),
                        event.getNewStatus(),
                        event.getChangedBy(),
                        event.getChangedAt()
                ))
                .toList();
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
