
package com.transformatives.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.transformatives.backend.entity.ShipmentAssignmentHistory;

public interface ShipmentAssignmentHistoryRepository
        extends JpaRepository<ShipmentAssignmentHistory, Long> {

    List<ShipmentAssignmentHistory>
            findByTrackingNumberOrderByAssignedAtDescIdDesc(
                    String trackingNumber
            );
}
