
package com.transformatives.backend.service;

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

import com.transformatives.backend.dto.ShipmentResponse;
import com.transformatives.backend.entity.Shipment;
import com.transformatives.backend.entity.ShipmentStatusHistory;
import com.transformatives.backend.repository.ShipmentRepository;
import com.transformatives.backend.repository.ShipmentStatusHistoryRepository;

class ShipmentCancellationTest {

    private static final String TRACKING =
            "TRF-1234567890ABCDEF";

    private ShipmentRepository repository;
    private ShipmentStatusHistoryRepository historyRepository;
    private ShipmentService service;

    @BeforeEach
    void setUp() {
        repository = mock(ShipmentRepository.class);
        historyRepository = mock(
                ShipmentStatusHistoryRepository.class
        );

        service = new ShipmentService(
                repository,
                historyRepository
        );
    }

    private Shipment cancelledShipment() {
        Shipment shipment = new Shipment(
                TRACKING,
                "customer01",
                "Test Recipient",
                "Dublin",
                "Dublin 1",
                "+353 871234567",
                "D02 X285",
                "STANDARD_PARCEL"
        );

        shipment.cancel();
        return shipment;
    }

    @Test
    void ownerCanCancelPendingShipment() {
        when(repository.cancelPendingForOwner(
                TRACKING,
                "customer01"
        )).thenReturn(1);

        when(repository.findByTrackingNumberAndOwnerUsername(
                TRACKING,
                "customer01"
        )).thenReturn(Optional.of(cancelledShipment()));

        ShipmentResponse result = service.cancel(
                TRACKING,
                "customer01"
        );

        assertEquals("CANCELLED", result.status());

        verify(repository).cancelPendingForOwner(
                TRACKING,
                "customer01"
        );

        verify(historyRepository).save(
                any(ShipmentStatusHistory.class)
        );
    }

    @Test
    void anotherCustomerCannotCancelShipment() {
        when(repository.cancelPendingForOwner(
                TRACKING,
                "otherCustomer"
        )).thenReturn(0);

        when(repository.findByTrackingNumberAndOwnerUsername(
                TRACKING,
                "otherCustomer"
        )).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.cancel(
                        TRACKING,
                        "otherCustomer"
                )
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(historyRepository, never()).save(
                any(ShipmentStatusHistory.class)
        );
    }

    @Test
    void cancelledShipmentCannotBeCancelledAgain() {
        when(repository.cancelPendingForOwner(
                TRACKING,
                "customer01"
        )).thenReturn(0);

        when(repository.findByTrackingNumberAndOwnerUsername(
                TRACKING,
                "customer01"
        )).thenReturn(Optional.of(cancelledShipment()));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.cancel(
                        TRACKING,
                        "customer01"
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(historyRepository, never()).save(
                any(ShipmentStatusHistory.class)
        );
    }

    @Test
    void successfulCancellationRecordsCorrectAuditDetails() {
        when(repository.cancelPendingForOwner(
                TRACKING,
                "customer01"
        )).thenReturn(1);

        when(repository.findByTrackingNumberAndOwnerUsername(
                TRACKING,
                "customer01"
        )).thenReturn(Optional.of(cancelledShipment()));

        service.cancel(TRACKING, "customer01");

        var captor = org.mockito.ArgumentCaptor
                .forClass(ShipmentStatusHistory.class);

        verify(historyRepository).save(captor.capture());

        ShipmentStatusHistory event = captor.getValue();

        assertEquals(TRACKING, event.getTrackingNumber());
        assertEquals("PENDING", event.getOldStatus());
        assertEquals("CANCELLED", event.getNewStatus());
        assertEquals("customer01", event.getChangedBy());
    }

    @Test
    void failedAtomicUpdateCannotCreateAuditEvent() {
        when(repository.cancelPendingForOwner(
                TRACKING,
                "customer01"
        )).thenReturn(0);

        when(repository.findByTrackingNumberAndOwnerUsername(
                TRACKING,
                "customer01"
        )).thenReturn(Optional.of(cancelledShipment()));

        assertThrows(
                ResponseStatusException.class,
                () -> service.cancel(TRACKING, "customer01")
        );

        verify(historyRepository, never()).save(
                any(ShipmentStatusHistory.class)
        );
    }
}
