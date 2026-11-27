
package com.transformatives.backend.entity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "shipment_assignment_history")
public class ShipmentAssignmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_number", nullable = false, length = 24)
    private String trackingNumber;

    @Column(name = "courier_username", nullable = false, length = 100)
    private String courierUsername;

    @Column(name = "assigned_by", nullable = false, length = 100)
    private String assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    protected ShipmentAssignmentHistory() {
    }

    public ShipmentAssignmentHistory(
            String trackingNumber,
            String courierUsername,
            String assignedBy) {

        this.trackingNumber = trackingNumber;
        this.courierUsername = courierUsername;
        this.assignedBy = assignedBy;
        this.assignedAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getCourierUsername() {
        return courierUsername;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
}
