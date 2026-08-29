package com.smartparking.parking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParkingEntryRequest(

        @NotBlank(message = "Plate number is required")
        @Size(max = 20, message = "Plate number cannot exceed 20 characters")
        String plateNumber

) {
}