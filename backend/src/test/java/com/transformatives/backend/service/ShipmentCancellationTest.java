
package com.transformatives.backend.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.transformatives.backend.dto.ShipmentResponse;
import com.transformatives.backend.entity.Shipment;
import com.transformatives.backend.repository.ShipmentRepository;

class ShipmentCancellationTest {

    private ShipmentRepository repository;
    private ShipmentService service;

    @BeforeEach
    void setUp() {
        repository = mock(ShipmentRepository.class);
        service = new ShipmentService(repository);
    }

    private Shipment newShipment() {
        return new Shipment(
                "TRF-1234567890ABCDEF",
                "customer01",
                "Test Recipient",
                "Dublin",
                "Dublin 1"
        );
    }

    @Test
    void ownerCanCancelPendingShipment() {
        Shipment shipment = newShipment();

        when(repository.findByTrackingNumberAndOwnerUsername(
                "TRF-1234567890ABCDEF",
                "customer01"
        )).thenReturn(Optional.of(shipment));

        ShipmentResponse result = service.cancel(
                "TRF-1234567890ABCDEF",
                "customer01"
        );

        assertEquals("CANCELLED", result.status());
        assertEquals("CANCELLED", shipment.getStatus());
    }

    @Test
    void anotherCustomerCannotCancelShipment() {
        when(repository.findByTrackingNumberAndOwnerUsername(
                "TRF-1234567890ABCDEF",
                "otherCustomer"
        )).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.cancel(
                        "TRF-1234567890ABCDEF",
                        "otherCustomer"
                )
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    void cancelledShipmentCannotBeCancelledAgain() {
        Shipment shipment = newShipment();
        shipment.cancel();

        when(repository.findByTrackingNumberAndOwnerUsername(
                "TRF-1234567890ABCDEF",
                "customer01"
        )).thenReturn(Optional.of(shipment));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.cancel(
                        "TRF-1234567890ABCDEF",
                        "customer01"
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );
    }
}
