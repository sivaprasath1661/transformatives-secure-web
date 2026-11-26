
package com.transformatives.backend.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.web.server.ResponseStatusException;

import com.transformatives.backend.dto.ShipmentRequest;
import com.transformatives.backend.entity.Shipment;
import com.transformatives.backend.repository.ShipmentRepository;

class ShipmentServiceTest {

    private ShipmentRepository repository;
    private ShipmentService service;

    @BeforeEach
    void setUp() {
        repository = mock(ShipmentRepository.class);
        service = new ShipmentService(repository);
    }

    @Test
    void createShipmentShouldAssignAuthenticatedOwner() {
        when(repository.save(any(Shipment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var request = new ShipmentRequest(
                "John",
                "Galway",
                "12 Main Street, Galway",
                "+353 871234567",
                "H91 ABC1",
                "STANDARD_PARCEL"
        );

        var result = service.create(request, "testcustomer01");

        var captor = org.mockito.ArgumentCaptor
                .forClass(Shipment.class);

        verify(repository).save(captor.capture());

        assertEquals(
                "testcustomer01",
                captor.getValue().getOwnerUsername()
        );

        assertEquals("John", result.recipientName());
        assertEquals("+353 871234567", result.recipientPhone());
        assertEquals("H91 ABC1", result.postalCode());
        assertEquals("STANDARD_PARCEL", result.packageType());
        assertEquals("PENDING", result.status());

        assertTrue(result.trackingNumber().startsWith("TRF-"));
    }

    @Test
    void shipmentListShouldFilterByAuthenticatedOwner() {
        when(repository.findByOwnerUsernameOrderByIdDesc(
                "testcustomer01"
        )).thenReturn(List.of());

        service.findMyShipments("testcustomer01");

        verify(repository)
                .findByOwnerUsernameOrderByIdDesc(
                        "testcustomer01"
                );
    }

    @Test
    void missingOrUnownedShipmentShouldReturn404() {
        when(repository.findByTrackingNumberAndOwnerUsername(
                "TRF-OTHER",
                "testcustomer01"
        )).thenReturn(Optional.empty());

        var exception = assertThrows(
                ResponseStatusException.class,
                () -> service.track(
                        "TRF-OTHER",
                        "testcustomer01"
                )
        );

        assertEquals(
                404,
                exception.getStatusCode().value()
        );
    }
}
