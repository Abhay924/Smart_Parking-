package com.smartparking.verification;

import java.time.OffsetDateTime;

public record VehicleVerificationResponse(
    Long id, String plateNumber, String status, String source,
    String captureSource, OffsetDateTime verifiedAt, String message
) {}
