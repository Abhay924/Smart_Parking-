package com.smartparking.vehicle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateVehicleRequest(

        @NotBlank(message = "Plate number is required")
        @Size(max = 20, message = "Plate number cannot exceed 20 characters")
        @Pattern(
            regexp = "^[A-Za-z0-9 -]+$",
            message = "Invalid plate number format"
        )
        String plateNumber
) {
}