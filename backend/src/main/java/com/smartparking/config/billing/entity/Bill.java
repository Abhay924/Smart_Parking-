package com.smartparking.billing.entity;

import com.smartparking.parking.entity.ParkingSession;
import com.smartparking.parkingrate.entity.ParkingRate;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "bills",
        indexes = {
                @Index(name = "idx_bill_session", columnList = "session_id"),
                @Index(name = "idx_bill_status", columnList = "status")
        }
)
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "session_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_bill_session")
    )
    private ParkingSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "rate_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_bill_rate")
    )
    private ParkingRate rate;

    @Column(name = "duration_minutes", nullable = false)
    private Long durationMinutes;

    @Column(name = "grace_minutes", nullable = false)
    private Integer graceMinutes;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Bill() {
    }

    public Bill(
            ParkingSession session,
            ParkingRate rate,
            Long durationMinutes,
            Integer graceMinutes,
            BigDecimal amount
    ) {
        this.session = session;
        this.rate = rate;
        this.durationMinutes = durationMinutes;
        this.graceMinutes = graceMinutes;
        this.amount = amount;
        this.status = BillStatus.UNPAID;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public ParkingSession getSession() {
        return session;
    }

    public ParkingRate getRate() {
        return rate;
    }

    public Long getDurationMinutes() {
        return durationMinutes;
    }

    public Integer getGraceMinutes() {
        return graceMinutes;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BillStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void markPaid() {
        this.status = BillStatus.PAID;
    }

    public void cancel() {
        this.status = BillStatus.CANCELLED;
    }
}