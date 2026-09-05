package com.smartparking.parkingrate.dto;

import com.smartparking.parkingrate.entity.BillingMode;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ParkingRateResponse(

        Long id,

        String name,

        String vehicleType,

        BillingMode billingMode,

        BigDecimal ratePerUnit,

        BigDecimal minimumCharge,

        Integer gracePeriodMinutes,

        BigDecimal maximumDailyCharge,

        Boolean active,

        OffsetDateTime createdAt
) {
}