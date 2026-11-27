
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

import com.transformatives.backend.entity.Shipment;
import com.transformatives.backend.entity.ShipmentAssignmentHistory;
import com.transformatives.backend.entity.User;
import com.transformatives.backend.repository.ShipmentAssignmentHistoryRepository;
import com.transformatives.backend.repository.ShipmentRepository;
import com.transformatives.backend.repository.UserRepository;

class CourierAssignmentServiceTest {

    private static final String TRACKING =
            "TRF-1234567890ABCDEF";

    private ShipmentRepository shipments;
    private UserRepository users;
    private ShipmentAssignmentHistoryRepository audit;
    private CourierAssignmentService service;

    @BeforeEach
    void setup() {
        shipments = mock(ShipmentRepository.class);
        users = mock(UserRepository.class);
        audit = mock(ShipmentAssignmentHistoryRepository.class);

        service = new CourierAssignmentService(
                shipments,
                users,
                audit
        );
    }

    @Test
    void canAssignVerifiedCourierAndRecordAudit() {
        when(users.findByUsername("jetdeliveries"))
                .thenReturn(Optional.of(
                        new User(
                                "Jet Deliveries",
                                "jetdeliveries",
                                "jet@example.com",
                                "hash",
                                "COURIER"
                        )
                ));

        when(shipments.assignIfPendingAndUnassigned(
                TRACKING, "jetdeliveries"
        )).thenReturn(1);

        Shipment updated = mock(Shipment.class);

        when(updated.getAssignedCourier())
                .thenReturn("jetdeliveries");

        when(shipments.findByTrackingNumber(TRACKING))
                .thenReturn(Optional.of(updated));

        assertEquals(
                "jetdeliveries",
                service.assign(
                        TRACKING,
                        "jetdeliveries",
                        "logisticsadmin01"
                ).assignedCourier()
        );

        verify(audit).save(
                any(ShipmentAssignmentHistory.class)
        );
    }

    @Test
    void rejectsUnapprovedCourierUsername() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.assign(
                        TRACKING,
                        "attacker",
                        "logisticsadmin01"
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                error.getStatusCode()
        );

        verify(audit, never()).save(
                any(ShipmentAssignmentHistory.class)
        );
    }

    @Test
    void cannotAssignUnregisteredOrCustomerAccount() {
        when(users.findByUsername("jkcouriers"))
                .thenReturn(Optional.of(
                        new User(
                                "Test",
                                "jkcouriers",
                                "jk@example.com",
                                "hash",
                                "CUSTOMER"
                        )
                ));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.assign(
                        TRACKING,
                        "jkcouriers",
                        "logisticsadmin01"
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                error.getStatusCode()
        );
    }

    @Test
    void cannotOverwriteExistingAssignment() {
        when(users.findByUsername("jkcouriers"))
                .thenReturn(Optional.of(
                        new User(
                                "JK Couriers",
                                "jkcouriers",
                                "jk@example.com",
                                "hash",
                                "COURIER"
                        )
                ));

        when(shipments.findByTrackingNumber(TRACKING))
                .thenReturn(Optional.of(mock(Shipment.class)));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.assign(
                        TRACKING,
                        "jkcouriers",
                        "logisticsadmin01"
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                error.getStatusCode()
        );

        verify(audit, never()).save(
                any(ShipmentAssignmentHistory.class)
        );
    }
}
