package com.smartparking.parking.dto;

import java.time.OffsetDateTime;

public record ParkingEntryResponse(
        Long sessionId,
        Long vehicleId,
        String plateNumber,
        OffsetDateTime entryTime,
        String status
) {
}