
package com.transformatives.backend.dto;

public record ShipmentResponse(
        Long id,
        String trackingNumber,
        String recipientName,
        String destinationCity,
        String deliveryAddress,
        String status
) {}
