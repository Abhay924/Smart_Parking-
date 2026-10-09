package com.smartparking.verification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VehicleVerificationRequest(
    @NotBlank @Pattern(regexp = "^[A-Za-z0-9 -]{1,20}$") String plateNumber,
    @NotBlank @Pattern(regexp = "^(MANUAL|ANPR)$") String captureSource
) {}
