
package com.transformatives.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.transformatives.backend.entity.Shipment;

public interface ShipmentRepository
        extends JpaRepository<Shipment, Long> {

    List<Shipment> findByOwnerUsernameOrderByIdDesc(
            String ownerUsername
    );

    Optional<Shipment> findByTrackingNumberAndOwnerUsername(
            String trackingNumber,
            String ownerUsername
    );

    List<Shipment> findAllByOrderByIdDesc();

    Optional<Shipment> findByTrackingNumber(
            String trackingNumber
    );

    List<Shipment> findByAssignedCourierOrderByIdDesc(
            String assignedCourier
    );

    Optional<Shipment> findByTrackingNumberAndAssignedCourier(
            String trackingNumber,
            String assignedCourier
    );

    // Existing administrator conditional status update.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Shipment s
            SET s.status = :newStatus
            WHERE s.trackingNumber = :trackingNumber
              AND s.status = :expectedStatus
            """)
    int updateStatusIfCurrent(
            @Param("trackingNumber") String trackingNumber,
            @Param("expectedStatus") String expectedStatus,
            @Param("newStatus") String newStatus
    );

    // Existing secure customer cancellation.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Shipment s
            SET s.status = 'CANCELLED'
            WHERE s.trackingNumber = :trackingNumber
              AND s.ownerUsername = :ownerUsername
              AND s.status = 'PENDING'
            """)
    int cancelPendingForOwner(
            @Param("trackingNumber") String trackingNumber,
            @Param("ownerUsername") String ownerUsername
    );

    // Assign only pending and unassigned shipments.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Shipment s
            SET s.assignedCourier = :courier
            WHERE s.trackingNumber = :trackingNumber
              AND s.status = 'PENDING'
              AND s.assignedCourier IS NULL
            """)
    int assignIfPendingAndUnassigned(
            @Param("trackingNumber") String trackingNumber,
            @Param("courier") String courier
    );

    // Courier status update with ownership enforced in SQL.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Shipment s
            SET s.status = :newStatus
            WHERE s.trackingNumber = :trackingNumber
              AND s.assignedCourier = :courier
              AND s.status = :expectedStatus
            """)
    int updateStatusForCourier(
            @Param("trackingNumber") String trackingNumber,
            @Param("courier") String courier,
            @Param("expectedStatus") String expectedStatus,
            @Param("newStatus") String newStatus
    );
}
