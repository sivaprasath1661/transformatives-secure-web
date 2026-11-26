
package com.transformatives.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.transformatives.backend.entity.ShipmentStatusHistory;

public interface ShipmentStatusHistoryRepository
        extends JpaRepository<ShipmentStatusHistory, Long> {

    List<ShipmentStatusHistory>
            findByTrackingNumberOrderByChangedAtDescIdDesc(
                    String trackingNumber
            );
}
