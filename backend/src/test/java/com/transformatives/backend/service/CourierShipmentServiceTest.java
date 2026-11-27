
package com.transformatives.backend.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.transformatives.backend.entity.Shipment;
import com.transformatives.backend.entity.ShipmentStatusHistory;
import com.transformatives.backend.repository.ShipmentRepository;
import com.transformatives.backend.repository.ShipmentStatusHistoryRepository;

class CourierShipmentServiceTest {

    private static final String TRACKING =
            "TRF-1234567890ABCDEF";

    private ShipmentRepository shipments;
    private ShipmentStatusHistoryRepository history;
    private CourierShipmentService service;

    @BeforeEach
    void setup() {
        shipments = mock(ShipmentRepository.class);
        history = mock(ShipmentStatusHistoryRepository.class);

        service = new CourierShipmentService(
                shipments,
                history
        );
    }

    @Test
    void courierListIsAlwaysFilteredByAuthenticatedUsername() {
        when(shipments.findByAssignedCourierOrderByIdDesc(
                "jetdeliveries"
        )).thenReturn(List.of());

        assertEquals(
                0,
                service.myShipments("jetdeliveries").size()
        );

        verify(shipments)
                .findByAssignedCourierOrderByIdDesc(
                        "jetdeliveries"
                );
    }

    @Test
    void assignedCourierCanStartDeliveryAndIsAudited() {
        when(shipments.updateStatusForCourier(
                TRACKING,
                "jetdeliveries",
                "PENDING",
                "IN_TRANSIT"
        )).thenReturn(1);

        Shipment updated = mock(Shipment.class);
        when(updated.getStatus()).thenReturn("IN_TRANSIT");

        when(shipments.findByTrackingNumberAndAssignedCourier(
                TRACKING,
                "jetdeliveries"
        )).thenReturn(Optional.of(updated));

        assertEquals(
                "IN_TRANSIT",
                service.updateStatus(
                        TRACKING,
                        "IN_TRANSIT",
                        "jetdeliveries"
                ).status()
        );

        verify(history).save(
                any(ShipmentStatusHistory.class)
        );
    }

    @Test
    void otherCourierCannotUpdateShipment() {
        when(shipments.updateStatusForCourier(
                TRACKING,
                "jkcouriers",
                "PENDING",
                "IN_TRANSIT"
        )).thenReturn(0);

        when(shipments.findByTrackingNumberAndAssignedCourier(
                TRACKING,
                "jkcouriers"
        )).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.updateStatus(
                        TRACKING,
                        "IN_TRANSIT",
                        "jkcouriers"
                )
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                error.getStatusCode()
        );

        verify(history, never()).save(
                any(ShipmentStatusHistory.class)
        );
    }

    @Test
    void courierCannotSkipDirectlyToDelivered() {
        when(shipments.findByTrackingNumberAndAssignedCourier(
                TRACKING,
                "jetdeliveries"
        )).thenReturn(Optional.of(mock(Shipment.class)));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.updateStatus(
                        TRACKING,
                        "DELIVERED",
                        "jetdeliveries"
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                error.getStatusCode()
        );

        verify(history, never()).save(
                any(ShipmentStatusHistory.class)
        );
    }

    @Test
    void arbitraryStatusIsRejected() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.updateStatus(
                        TRACKING,
                        "CANCELLED",
                        "jetdeliveries"
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                error.getStatusCode()
        );
    }
}
