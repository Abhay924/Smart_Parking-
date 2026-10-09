package com.smartparking.anpr.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnprDetectionRequest(

        @NotBlank(message = "Plate number is required")
        @Size(max = 20, message = "Plate number cannot exceed 20 characters")
        String plateNumber,

        @DecimalMin(value = "0.0", message = "Confidence cannot be less than 0")
        @DecimalMax(value = "1.0", message = "Confidence cannot exceed 1")
        Double confidence,

        @Size(max = 255, message = "Image path cannot exceed 255 characters")
        String imagePath,

        @NotBlank(message = "Camera ID is required")
        @Size(max = 100, message = "Camera ID cannot exceed 100 characters")
        String cameraId
) {
}