
package com.transformatives.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 24)
    private String trackingNumber;

    @Column(nullable = false, length = 100)
    private String ownerUsername;

    @Column(nullable = false, length = 100)
    private String recipientName;

    @Column(nullable = false, length = 100)
    private String destinationCity;

    @Column(nullable = false, length = 255)
    private String deliveryAddress;

    @Column(nullable = false, length = 20)
    private String status;

    protected Shipment() {
        // Required by JPA
    }

    public Shipment(
            String trackingNumber,
            String ownerUsername,
            String recipientName,
            String destinationCity,
            String deliveryAddress) {

        this.trackingNumber = trackingNumber;
        this.ownerUsername = ownerUsername;
        this.recipientName = recipientName;
        this.destinationCity = destinationCity;
        this.deliveryAddress = deliveryAddress;
        this.status = "PENDING";
    }

    public Long getId() {
        return id;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getDestinationCity() {
        return destinationCity;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getStatus() {
        return status;
    }
}
