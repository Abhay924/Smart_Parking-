package com.smartparking.parking.dto;

import java.time.OffsetDateTime;

public record ParkingExitResponse(
        Long sessionId,
        Long vehicleId,
        String plateNumber,
        OffsetDateTime entryTime,
        OffsetDateTime exitTime,
        long durationMinutes,
        String status
) {
}