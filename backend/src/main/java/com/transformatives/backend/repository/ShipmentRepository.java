
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
}
