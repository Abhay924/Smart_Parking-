package com.smartparking.anpr.dto;

import java.time.OffsetDateTime;

public record AnprDetectionResponse(
        Long vehicleId,
        Long parkingSessionId,
        String plateNumber,
        Double confidence,
        String cameraId,
        String imagePath,
        String status,
        OffsetDateTime detectedAt
) {
}