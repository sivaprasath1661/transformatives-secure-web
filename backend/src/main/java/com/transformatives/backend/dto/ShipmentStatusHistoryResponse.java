
package com.transformatives.backend.dto;

import java.time.LocalDateTime;

public record ShipmentStatusHistoryResponse(
        Long id,
        String trackingNumber,
        String oldStatus,
        String newStatus,
        String changedBy,
        LocalDateTime changedAt
) {}
