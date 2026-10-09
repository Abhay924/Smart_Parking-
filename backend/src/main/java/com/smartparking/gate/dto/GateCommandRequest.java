package com.smartparking.gate.dto;

import com.smartparking.gate.entity.GateDirection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GateCommandRequest(

        @NotBlank(message = "Gate ID is required")
        String gateId,

        @NotNull(message = "Direction is required")
        GateDirection direction,

        @NotNull(message = "Parking session ID is required")
        Long parkingSessionId,

        Long billId
) {
}