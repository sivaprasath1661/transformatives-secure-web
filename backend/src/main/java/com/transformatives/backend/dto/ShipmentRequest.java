
package com.transformatives.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShipmentRequest(

        @NotBlank
        @Size(max = 100)
        String recipientName,

        @NotBlank
        @Size(max = 100)
        String destinationCity,

        @NotBlank
        @Size(max = 255)
        String deliveryAddress

) {}
