
package com.transformatives.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CourierAssignmentRequest(

        @NotBlank
        @Pattern(
                regexp = "jetdeliveries|jkcouriers",
                message = "Select a valid courier"
        )
        String courierUsername

) {}
