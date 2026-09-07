package com.smartparking.billing.service;

import com.smartparking.billing.dto.BillResponse;
import com.smartparking.billing.entity.Bill;
import com.smartparking.billing.repository.BillRepository;
import com.smartparking.parking.entity.ParkingSession;
import com.smartparking.parking.entity.ParkingSessionStatus;
import com.smartparking.parking.repository.ParkingSessionRepository;
import com.smartparking.parkingrate.entity.ParkingRate;
import com.smartparking.parkingrate.repository.ParkingRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class BillingService {

    private final BillRepository billRepository;
    private final ParkingSessionRepository parkingSessionRepository;
    private final ParkingRateRepository parkingRateRepository;

    public BillingService(
            BillRepository billRepository,
            ParkingSessionRepository parkingSessionRepository,
            ParkingRateRepository parkingRateRepository
    ) {
        this.billRepository = billRepository;
        this.parkingSessionRepository = parkingSessionRepository;
        this.parkingRateRepository = parkingRateRepository;
    }

    @Transactional
    public Bill generateBill(Long sessionId) {

        ParkingSession session = parkingSessionRepository
                .findById(sessionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parking session not found: " + sessionId
                        )
                );

        if (session.getStatus() != ParkingSessionStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Parking session must be completed before billing"
            );
        }

        if (session.getExitTime() == null) {
            throw new IllegalStateException(
                    "Exit time is missing for parking session: " + sessionId
            );
        }

        if (billRepository.findBySessionId(sessionId).isPresent()) {
            throw new IllegalStateException(
                    "Bill already exists for parking session: " + sessionId
            );
        }

        ParkingRate rate = parkingRateRepository
                .findFirstByVehicleTypeAndActiveTrueOrderByIdDesc("CAR")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No active parking rate configured"
                        )
                );

        long durationMinutes = calculateDurationMinutes(
                session.getEntryTime(),
                session.getExitTime()
        );

        BigDecimal baseAmount = calculateAmount(
                durationMinutes,
                rate
        );

        BigDecimal taxAmount = BigDecimal.ZERO.setScale(
                2,
                RoundingMode.HALF_UP
        );

        BigDecimal discountAmount = BigDecimal.ZERO.setScale(
                2,
                RoundingMode.HALF_UP
        );

        BigDecimal totalAmount = baseAmount
                .add(taxAmount)
                .subtract(discountAmount)
                .setScale(2, RoundingMode.HALF_UP);

        String billNumber = generateBillNumber();

       Bill bill = new Bill(
        billNumber,
        session,
        rate,
        durationMinutes,
        rate.getGracePeriodMinutes(),
        baseAmount,
        taxAmount,
        discountAmount,
        totalAmount
);

        return billRepository.save(bill);
    }

    @Transactional
    public BillResponse generateBillResponse(Long sessionId) {
        Bill bill = generateBill(sessionId);
        return BillResponse.from(bill);
    }

    private long calculateDurationMinutes(
            OffsetDateTime entryTime,
            OffsetDateTime exitTime
    ) {
        if (entryTime == null) {
            throw new IllegalStateException("Entry time is missing");
        }

        if (exitTime == null) {
            throw new IllegalStateException("Exit time is missing");
        }

        if (exitTime.isBefore(entryTime)) {
            throw new IllegalStateException(
                    "Exit time cannot be before entry time"
            );
        }

        return Duration.between(entryTime, exitTime).toMinutes();
    }

    private BigDecimal calculateAmount(
            long durationMinutes,
            ParkingRate rate
    ) {
        if (durationMinutes < 0) {
            throw new IllegalArgumentException(
                    "Duration cannot be negative"
            );
        }

        if (rate.getGracePeriodMinutes() == null
                || rate.getGracePeriodMinutes() < 0) {
            throw new IllegalStateException(
                    "Invalid grace period configuration"
            );
        }

        if (rate.getRatePerUnit() == null
                || rate.getRatePerUnit()
                .compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "Invalid parking rate configuration"
            );
        }

        long graceMinutes = rate.getGracePeriodMinutes();

        if (durationMinutes <= graceMinutes) {
            return BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }

        long billableMinutes = durationMinutes - graceMinutes;

        BigDecimal amount;

        switch (rate.getBillingMode()) {

            case PER_MINUTE -> {
                amount = rate.getRatePerUnit()
                        .multiply(
                                BigDecimal.valueOf(billableMinutes)
                        );
            }

            case PER_HOUR -> {
                long billableHours =
                        (billableMinutes + 59) / 60;

                amount = rate.getRatePerUnit()
                        .multiply(
                                BigDecimal.valueOf(billableHours)
                        );
            }

            case FLAT_RATE -> {
                amount = rate.getRatePerUnit();
            }

            default -> throw new IllegalStateException(
                    "Unsupported billing mode: "
                            + rate.getBillingMode()
            );
        }

        if (rate.getMinimumCharge() != null
                && amount.compareTo(BigDecimal.ZERO) > 0
                && amount.compareTo(rate.getMinimumCharge()) < 0) {

            amount = rate.getMinimumCharge();
        }

        if (rate.getMaximumDailyCharge() != null
                && amount.compareTo(rate.getMaximumDailyCharge()) > 0) {

            amount = rate.getMaximumDailyCharge();
        }

        return amount.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private String generateBillNumber() {

        String timestamp = OffsetDateTime.now()
                .format(
                        DateTimeFormatter.ofPattern(
                                "yyyyMMddHHmmss"
                        )
                );

        String randomPart = UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();

        return "BILL-" + timestamp + "-" + randomPart;
    }
}