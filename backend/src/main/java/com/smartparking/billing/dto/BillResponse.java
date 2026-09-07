package com.smartparking.billing.dto;

import com.smartparking.billing.entity.Bill;
import com.smartparking.billing.entity.BillStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BillResponse(
        Long id,
        String billNumber,
        Long sessionId,
        Long vehicleId,
        String plateNumber,
        Long durationMinutes,
        Integer graceMinutes,
        BigDecimal baseAmount,
        BigDecimal taxAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String currency,
        BillStatus status,
        OffsetDateTime generatedAt,
        OffsetDateTime paidAt
) {

    public static BillResponse from(Bill bill) {
        return new BillResponse(
                bill.getId(),
                bill.getBillNumber(),
                bill.getSession().getId(),
                bill.getSession().getVehicle().getId(),
                bill.getSession().getVehicle().getPlateNumber(),
                bill.getDurationMinutes(),
                bill.getGraceMinutes(),
                bill.getBaseAmount(),
                bill.getTaxAmount(),
                bill.getDiscountAmount(),
                bill.getTotalAmount(),
                bill.getCurrency(),
                bill.getStatus(),
                bill.getGeneratedAt(),
                bill.getPaidAt()
        );
    }
}