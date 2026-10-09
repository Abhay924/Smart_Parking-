package com.smartparking.gate.service;

import com.smartparking.billing.entity.Bill;
import com.smartparking.billing.entity.BillStatus;
import com.smartparking.billing.repository.BillRepository;
import com.smartparking.gate.dto.GateCommandRequest;
import com.smartparking.gate.entity.GateAction;
import com.smartparking.gate.entity.GateDirection;
import com.smartparking.gate.entity.GateEvent;
import com.smartparking.gate.repository.GateEventRepository;
import com.smartparking.gate.simulator.GateSimulator;
import com.smartparking.parking.entity.ParkingSession;
import com.smartparking.parking.entity.ParkingSessionStatus;
import com.smartparking.parking.repository.ParkingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GateService {

    private final GateEventRepository gateEventRepository;
    private final ParkingSessionRepository parkingSessionRepository;
    private final BillRepository billRepository;
    private final GateSimulator gateSimulator;

    public GateService(
            GateEventRepository gateEventRepository,
            ParkingSessionRepository parkingSessionRepository,
            BillRepository billRepository,
            GateSimulator gateSimulator
    ) {
        this.gateEventRepository = gateEventRepository;
        this.parkingSessionRepository = parkingSessionRepository;
        this.billRepository = billRepository;
        this.gateSimulator = gateSimulator;
    }

    @Transactional
    public GateEvent openGate(GateCommandRequest request) {

        UUID correlationId = UUID.randomUUID();

        ParkingSession session = parkingSessionRepository
                .findById(request.parkingSessionId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parking session not found: " + request.parkingSessionId()
                        ));

        if (request.direction() == GateDirection.ENTRY) {
            return processEntryGate(request, session, correlationId);
        }

        return processExitGate(request, session, correlationId);
    }

    private GateEvent processEntryGate(
            GateCommandRequest request,
            ParkingSession session,
            UUID correlationId
    ) {

        if (session.getStatus() != ParkingSessionStatus.ACTIVE) {
            return denyGate(
                    request,
                    correlationId,
                    "Entry gate denied because parking session is not ACTIVE"
            );
        }

        gateSimulator.open(request.gateId());

        return saveEvent(
                request,
                GateAction.OPEN,
                "OPENED",
                correlationId,
                "Entry validation successful"
        );
    }

    private GateEvent processExitGate(
            GateCommandRequest request,
            ParkingSession session,
            UUID correlationId
    ) {

        if (request.billId() == null) {
            return denyGate(
                    request,
                    correlationId,
                    "Exit gate denied because bill ID is required"
            );
        }

        Bill bill = billRepository
                .findById(request.billId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Bill not found: " + request.billId()
                        ));

        if (!bill.getSession().getId().equals(session.getId())) {
            return denyGate(
                    request,
                    correlationId,
                    "Exit gate denied because bill does not belong to parking session"
            );
        }

        if (session.getStatus() != ParkingSessionStatus.COMPLETED) {
            return denyGate(
                    request,
                    correlationId,
                    "Exit gate denied because parking session is not COMPLETED"
            );
        }

        if (bill.getStatus() != BillStatus.PAID) {
            return denyGate(
                    request,
                    correlationId,
                    "Exit gate denied because bill is not PAID"
            );
        }

        gateSimulator.open(request.gateId());

        return saveEvent(
                request,
                GateAction.OPEN,
                "OPENED",
                correlationId,
                "Payment verified successfully"
        );
    }

    @Transactional
    public GateEvent closeGate(
            String gateId,
            GateDirection direction,
            Long sessionId,
            Long billId
    ) {

        UUID correlationId = UUID.randomUUID();

        gateSimulator.close(gateId);

        GateEvent event = new GateEvent(
                gateId,
                direction,
                GateAction.CLOSE,
                "CLOSED",
                sessionId,
                billId,
                correlationId,
                "Gate closed"
        );

        return gateEventRepository.save(event);
    }

    @Transactional
    public GateEvent manualOpenGate(
            String gateId,
            GateDirection direction,
            Long sessionId,
            Long billId,
            String reason
    ) {

        validateOverrideReason(reason);

        UUID correlationId = UUID.randomUUID();

        gateSimulator.open(gateId);

        GateEvent event = new GateEvent(
                gateId,
                direction,
                GateAction.MANUAL_OPEN,
                "MANUAL",
                sessionId,
                billId,
                correlationId,
                reason
        );

        return gateEventRepository.save(event);
    }

    @Transactional
    public GateEvent emergencyOpenGate(
            String gateId,
            GateDirection direction,
            Long sessionId,
            Long billId,
            String reason
    ) {

        validateOverrideReason(reason);

        UUID correlationId = UUID.randomUUID();

        gateSimulator.open(gateId);

        GateEvent event = new GateEvent(
                gateId,
                direction,
                GateAction.EMERGENCY_OPEN,
                "EMERGENCY",
                sessionId,
                billId,
                correlationId,
                reason
        );

        return gateEventRepository.save(event);
    }

    private void validateOverrideReason(String reason) {

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Reason is required for gate override"
            );
        }

        if (reason.length() > 255) {
            throw new IllegalArgumentException(
                    "Reason cannot exceed 255 characters"
            );
        }
    }

    private GateEvent denyGate(
            GateCommandRequest request,
            UUID correlationId,
            String reason
    ) {

        gateSimulator.close(request.gateId());

        return saveEvent(
                request,
                GateAction.CLOSE,
                "DENIED",
                correlationId,
                reason
        );
    }

    private GateEvent saveEvent(
            GateCommandRequest request,
            GateAction action,
            String status,
            UUID correlationId,
            String reason
    ) {

        GateEvent event = new GateEvent(
                request.gateId(),
                request.direction(),
                action,
                status,
                request.parkingSessionId(),
                request.billId(),
                correlationId,
                reason
        );

        return gateEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public boolean isGateOpen(String gateId) {
        return gateSimulator.isOpen(gateId);
    }
}