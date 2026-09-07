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
                @Index(name = "idx_bills_generated_at", columnList = "generated_at"),
                @Index(name = "idx_bills_status", columnList = "status")
        }
)
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bill_number", nullable = false, unique = true, length = 50)
    private String billNumber;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "parking_session_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_bill_session")
    )
    private ParkingSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "rate_id",
            foreignKey = @ForeignKey(name = "fk_bill_rate")
    )
    private ParkingRate rate;

    @Column(name = "duration_minutes", nullable = false)
    private Long durationMinutes;

    @Column(name = "grace_minutes", nullable = false)
    private Integer graceMinutes;

    @Column(name = "base_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillStatus status = BillStatus.UNPAID;

    @Column(name = "generated_at", nullable = false)
    private OffsetDateTime generatedAt;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    protected Bill() {
    }

    public Bill(
            String billNumber,
            ParkingSession session,
            ParkingRate rate,
            Long durationMinutes,
            Integer graceMinutes,
            BigDecimal baseAmount,
            BigDecimal taxAmount,
            BigDecimal discountAmount,
            BigDecimal totalAmount
    ) {
        this.billNumber = billNumber;
        this.session = session;
        this.rate = rate;
        this.durationMinutes = durationMinutes;
        this.graceMinutes = graceMinutes;
        this.baseAmount = baseAmount;
        this.taxAmount = taxAmount;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;

        // Legacy DB column
        this.amount = totalAmount;

        this.currency = "INR";
        this.status = BillStatus.UNPAID;
    }

    @PrePersist
    protected void onCreate() {
        generatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getBillNumber() {
        return billNumber;
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

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public BillStatus getStatus() {
        return status;
    }

    public OffsetDateTime getGeneratedAt() {
        return generatedAt;
    }

    public OffsetDateTime getPaidAt() {
        return paidAt;
    }

    public void markPaid() {
        this.status = BillStatus.PAID;
        this.paidAt = OffsetDateTime.now();
    }

    public void cancel() {
        this.status = BillStatus.CANCELLED;
    }
}