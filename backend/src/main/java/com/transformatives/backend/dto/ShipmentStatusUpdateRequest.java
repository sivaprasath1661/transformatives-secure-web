
package com.transformatives.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ShipmentStatusUpdateRequest(

        @NotBlank
        @Pattern(
                regexp = "IN_TRANSIT|DELIVERED",
                message = "Status must be IN_TRANSIT or DELIVERED"
        )
        String status

) {}
