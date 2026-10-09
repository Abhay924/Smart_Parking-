package com.smartparking.gate.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "gate_events",
    indexes = {
        @Index(name = "idx_gate_events_gate", columnList = "gate_id"),
        @Index(name = "idx_gate_events_created", columnList = "created_at"),
        @Index(name = "idx_gate_events_correlation", columnList = "correlation_id")
    }
)
public class GateEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gate_id", nullable = false, length = 100)
    private String gateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 10)
    private GateDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private GateAction action;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "parking_session_id")
    private Long parkingSessionId;

    @Column(name = "bill_id")
    private Long billId;

    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected GateEvent() {
    }

    public GateEvent(
            String gateId,
            GateDirection direction,
            GateAction action,
            String status,
            Long parkingSessionId,
            Long billId,
            UUID correlationId,
            String reason
    ) {
        this.gateId = gateId;
        this.direction = direction;
        this.action = action;
        this.status = status;
        this.parkingSessionId = parkingSessionId;
        this.billId = billId;
        this.correlationId = correlationId;
        this.reason = reason;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }

        if (correlationId == null) {
            correlationId = UUID.randomUUID();
        }
    }

    public Long getId() {
        return id;
    }

    public String getGateId() {
        return gateId;
    }

    public GateDirection getDirection() {
        return direction;
    }

    public GateAction getAction() {
        return action;
    }

    public String getStatus() {
        return status;
    }

    public Long getParkingSessionId() {
        return parkingSessionId;
    }

    public Long getBillId() {
        return billId;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public String getReason() {
        return reason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}