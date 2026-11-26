
package com.transformatives.backend.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

class AdminShipmentServiceTest {

    private ShipmentRepository shipmentRepository;
    private ShipmentStatusHistoryRepository historyRepository;
    private AdminShipmentService service;

    @BeforeEach
    void setUp() {
        shipmentRepository = mock(ShipmentRepository.class);
        historyRepository = mock(ShipmentStatusHistoryRepository.class);

        service = new AdminShipmentService(
                shipmentRepository,
                historyRepository
        );
    }

    @Test
    void pendingShipmentCanMoveToInTransit() {
        String tracking = "TRF-1234567890ABCDEF";

        when(shipmentRepository.updateStatusIfCurrent(
                tracking, "PENDING", "IN_TRANSIT"
        )).thenReturn(1);

        Shipment updated = mock(Shipment.class);
        when(updated.getStatus()).thenReturn("IN_TRANSIT");

        when(shipmentRepository.findByTrackingNumber(tracking))
                .thenReturn(Optional.of(updated));

        var response = service.updateStatus(
                tracking,
                "IN_TRANSIT",
                "admin01"
        );

        assertEquals("IN_TRANSIT", response.status());

        ArgumentCaptor<ShipmentStatusHistory> captor =
                ArgumentCaptor.forClass(ShipmentStatusHistory.class);

        verify(historyRepository).save(captor.capture());

        assertEquals("admin01", captor.getValue().getChangedBy());
        assertEquals("PENDING", captor.getValue().getOldStatus());
        assertEquals("IN_TRANSIT", captor.getValue().getNewStatus());
    }

    @Test
    void inTransitShipmentCanMoveToDelivered() {
        String tracking = "TRF-1234567890ABCDEF";

        when(shipmentRepository.updateStatusIfCurrent(
                tracking, "IN_TRANSIT", "DELIVERED"
        )).thenReturn(1);

        Shipment updated = mock(Shipment.class);
        when(updated.getStatus()).thenReturn("DELIVERED");

        when(shipmentRepository.findByTrackingNumber(tracking))
                .thenReturn(Optional.of(updated));

        var response = service.updateStatus(
                tracking, "DELIVERED", "admin01"
        );

        assertEquals("DELIVERED", response.status());

        verify(shipmentRepository).updateStatusIfCurrent(
                tracking, "IN_TRANSIT", "DELIVERED"
        );
    }

    @Test
    void invalidTransitionReturnsConflict() {
        String tracking = "TRF-1234567890ABCDEF";

        when(shipmentRepository.updateStatusIfCurrent(
                tracking, "IN_TRANSIT", "DELIVERED"
        )).thenReturn(0);

        when(shipmentRepository.findByTrackingNumber(tracking))
                .thenReturn(Optional.of(mock(Shipment.class)));

        var exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateStatus(
                        tracking, "DELIVERED", "admin01"
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(historyRepository, never())
                .save(any(ShipmentStatusHistory.class));
    }

    @Test
    void missingShipmentReturnsNotFound() {
        String tracking = "TRF-NOTFOUND";

        when(shipmentRepository.findByTrackingNumber(tracking))
                .thenReturn(Optional.empty());

        var exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateStatus(
                        tracking, "IN_TRANSIT", "admin01"
                )
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    void unsupportedStatusReturnsBadRequest() {
        var exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateStatus(
                        "TRF-1234567890ABCDEF",
                        "CANCELLED",
                        "admin01"
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );
    }
}
