
package com.transformatives.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

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
}
