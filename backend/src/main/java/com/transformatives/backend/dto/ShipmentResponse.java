
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
        String packageType
) {
    // Compatibility with existing six-field test fixtures.
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
                null
        );
    }
}
