package com.smartparking.anpr.service;

import com.smartparking.anpr.dto.AnprDetectionRequest;
import com.smartparking.anpr.dto.AnprDetectionResponse;
import com.smartparking.gate.dto.GateCommandRequest;
import com.smartparking.gate.dto.GateResponse;
import com.smartparking.gate.entity.GateDirection;
import com.smartparking.gate.service.GateService;
import com.smartparking.parking.dto.ParkingEntryRequest;
import com.smartparking.parking.dto.ParkingEntryResponse;
import com.smartparking.parking.service.ParkingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Locale;

@Service
public class AnprService {

    private static final double MIN_CONFIDENCE = 0.85;
    private static final String ENTRY_GATE_ID = "ENTRY-GATE-01";

    private final ParkingService parkingService;
    private final GateService gateService;
    private final AnprEventLogService eventLogService;

    public AnprService(
            ParkingService parkingService,
            GateService gateService,
            AnprEventLogService eventLogService) {
        this.parkingService = parkingService;
        this.gateService = gateService;
        this.eventLogService = eventLogService;
    }

    @Transactional
    public AnprDetectionResponse processDetection(
            AnprDetectionRequest request) {

        long startedAt = System.nanoTime();

        String plateNumber = normalizePlate(request.plateNumber());

        Long eventId = eventLogService.recordDetection(
                plateNumber,
                request.confidence(),
                request.cameraId(),
                "ENTRY",
                request.imagePath()
        );

        try {
            validateConfidence(request.confidence());

            ParkingEntryRequest entryRequest =
                    new ParkingEntryRequest(plateNumber);

            ParkingEntryResponse parkingEntry =
                    parkingService.createEntry(entryRequest);

            GateCommandRequest gateRequest =
                    new GateCommandRequest(
                            ENTRY_GATE_ID,
                            GateDirection.ENTRY,
                            parkingEntry.sessionId(),
                            null
                    );

            GateResponse gateResponse =
                    GateResponse.from(gateService.openGate(gateRequest));

            if (!"OPENED".equals(gateResponse.status())) {
                throw new IllegalStateException(
                        "Entry gate was not opened: "
                                + gateResponse.reason()
                );
            }

            eventLogService.markAccepted(
                    eventId,
                    elapsedMillis(startedAt)
            );

            return new AnprDetectionResponse(
                    parkingEntry.vehicleId(),
                    parkingEntry.sessionId(),
                    parkingEntry.plateNumber(),
                    request.confidence(),
                    request.cameraId(),
                    request.imagePath(),
                    parkingEntry.status(),
                    OffsetDateTime.now()
            );

        } catch (RuntimeException exception) {
            try {
                eventLogService.markRejected(
                        eventId,
                        exception.getMessage(),
                        elapsedMillis(startedAt)
                );
            } catch (RuntimeException loggingException) {
                exception.addSuppressed(loggingException);
            }

            throw exception;
        }
    }

    private void validateConfidence(Double confidence) {
        if (confidence == null
                || !Double.isFinite(confidence)
                || confidence < MIN_CONFIDENCE
                || confidence > 1.0) {
            throw new IllegalArgumentException(
                    "ANPR confidence must be between "
                            + MIN_CONFIDENCE + " and 1.0"
            );
        }
    }

    private String normalizePlate(String plateNumber) {
        if (plateNumber == null || plateNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Plate number is required"
            );
        }

        return plateNumber.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}