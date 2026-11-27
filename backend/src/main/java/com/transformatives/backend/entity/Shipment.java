
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

    @Column(name = "recipient_phone", length = 25)
    private String recipientPhone;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "package_type", length = 30)
    private String packageType;

    @Column(name = "assigned_courier", length = 100)
    private String assignedCourier;

    protected Shipment() {
        // Required by JPA
    }

    public Shipment(
            String trackingNumber,
            String ownerUsername,
            String recipientName,
            String destinationCity,
            String deliveryAddress,
            String recipientPhone,
            String postalCode,
            String packageType) {

        this.trackingNumber = trackingNumber;
        this.ownerUsername = ownerUsername;
        this.recipientName = recipientName;
        this.destinationCity = destinationCity;
        this.deliveryAddress = deliveryAddress;
        this.recipientPhone = recipientPhone;
        this.postalCode = postalCode;
        this.packageType = packageType;
        this.status = "PENDING";
    }

    public Shipment(
            String trackingNumber,
            String ownerUsername,
            String recipientName,
            String destinationCity,
            String deliveryAddress) {

        this(
                trackingNumber,
                ownerUsername,
                recipientName,
                destinationCity,
                deliveryAddress,
                null,
                null,
                null
        );
    }

    public void cancel() {
        if (!"PENDING".equals(status)) {
            throw new IllegalStateException(
                    "Only pending shipments can be cancelled"
            );
        }

        status = "CANCELLED";
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

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getPackageType() {
        return packageType;
    }

    public String getAssignedCourier() {
        return assignedCourier;
    }
}
