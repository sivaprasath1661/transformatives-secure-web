
package com.transformatives.backend.dto;

public record ShipmentResponse(
        Long id,
        String trackingNumber,
        String recipientName,
        String destinationCity,
        String deliveryAddress,
        String status,
        String recipientPhone,
        String postalCode,
        String packageType,
        String assignedCourier
) {

    // Compatible with existing nine-field calls.
    public ShipmentResponse(
            Long id,
            String trackingNumber,
            String recipientName,
            String destinationCity,
            String deliveryAddress,
            String status,
            String recipientPhone,
            String postalCode,
            String packageType) {

        this(
                id,
                trackingNumber,
                recipientName,
                destinationCity,
                deliveryAddress,
                status,
                recipientPhone,
                postalCode,
                packageType,
                null
        );
    }

    // Compatible with older tests and callers.
    public ShipmentResponse(
            Long id,
            String trackingNumber,
            String recipientName,
            String destinationCity,
            String deliveryAddress,
            String status) {

        this(
                id,
                trackingNumber,
                recipientName,
                destinationCity,
                deliveryAddress,
                status,
                null,
                null,
                null,
                null
        );
    }
}
