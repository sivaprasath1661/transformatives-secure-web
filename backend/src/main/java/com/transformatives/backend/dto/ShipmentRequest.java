
package com.transformatives.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
        String deliveryAddress,

        @NotBlank
        @Size(max = 25)
        @Pattern(
                regexp = "^(?=(?:\\D*\\d){7,15}\\D*$)\\+?[0-9][0-9 ()-]*$",
                message = "Enter a valid phone number"
        )
        String recipientPhone,

        @NotBlank
        @Size(min = 3, max = 20)
        String postalCode,

        @NotBlank
        @Pattern(
                regexp = "DOCUMENTS|STANDARD_PARCEL|FRAGILE|ELECTRONICS|CLOTHING|OTHER",
                message = "Select a valid package type"
        )
        String packageType

) {
    // Retains source compatibility with earlier unit tests.
    // The old three-field request fails REST validation.
    public ShipmentRequest(
            String recipientName,
            String destinationCity,
            String deliveryAddress) {
        this(
                recipientName,
                destinationCity,
                deliveryAddress,
                null,
                null,
                null
        );
    }
}
