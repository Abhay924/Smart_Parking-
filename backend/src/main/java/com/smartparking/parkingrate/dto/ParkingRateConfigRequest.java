package com.smartparking.parkingrate.dto;

import com.smartparking.parkingrate.entity.BillingMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ParkingRateConfigRequest(

        @NotBlank(message = "Rate name is required")
        @Size(max = 100, message = "Rate name cannot exceed 100 characters")
        String name,

        @NotNull(message = "Billing mode is required")
        BillingMode billingMode,

        @NotNull(message = "Rate is required")
        @DecimalMin(value = "0.00", message = "Rate cannot be negative")
        BigDecimal ratePerUnit,

        @NotNull(message = "Minimum charge is required")
        @DecimalMin(value = "0.00", message = "Minimum charge cannot be negative")
        BigDecimal minimumCharge,

        @NotNull(message = "Grace period is required")
        @Min(value = 0, message = "Grace period cannot be negative")
        Integer gracePeriodMinutes,

        @DecimalMin(value = "0.00", message = "Maximum daily charge cannot be negative")
        BigDecimal maximumDailyCharge
) {
}