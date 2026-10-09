package com.smartparking.gate.dto;

import com.smartparking.gate.entity.GateEvent;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GateResponse(
        Long eventId,
        String gateId,
        String direction,
        String action,
        String status,
        Long parkingSessionId,
        Long billId,
        UUID correlationId,
        String reason,
        OffsetDateTime createdAt
) {

    public static GateResponse from(GateEvent event) {
        return new GateResponse(
                event.getId(),
                event.getGateId(),
                event.getDirection().name(),
                event.getAction().name(),
                event.getStatus(),
                event.getParkingSessionId(),
                event.getBillId(),
                event.getCorrelationId(),
                event.getReason(),
                event.getCreatedAt()
        );
    }
}