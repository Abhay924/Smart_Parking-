package com.smartparking.payment.entity;

import com.smartparking.billing.entity.Bill;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payments_bill", columnList = "bill_id"),
                @Index(name = "idx_payments_status", columnList = "status")
        }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "bill_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_payments_bill")
    )
    private Bill bill;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(name = "provider_payment_id", length = 150)
    private String providerPaymentId;

    @Column(name = "provider_order_id", length = 150)
    private String providerOrderId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.CREATED;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Column(name = "qr_reference", length = 150)
    private String qrReference;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Payment() {
    }

    public Payment(
            Bill bill,
            String provider,
            BigDecimal amount
    ) {
        this.bill = bill;
        this.provider = provider;
        this.amount = amount;
        this.currency = "INR";
        this.status = PaymentStatus.CREATED;
    }

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Bill getBill() {
        return bill;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderPaymentId() {
        return providerPaymentId;
    }

    public String getProviderOrderId() {
        return providerOrderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getQrReference() {
        return qrReference;
    }

    public OffsetDateTime getPaidAt() {
        return paidAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void markPending() {
        this.status = PaymentStatus.PENDING;
    }

    public void markSuccess(
            String providerPaymentId,
            String paymentMethod
    ) {
        this.providerPaymentId = providerPaymentId;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.SUCCESS;
        this.paidAt = OffsetDateTime.now();
    }

    public void markFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public void markCancelled() {
        this.status = PaymentStatus.CANCELLED;
    }

    public void markRefunded() {
        this.status = PaymentStatus.REFUNDED;
    }

    public void setProviderOrderId(String providerOrderId) {
        this.providerOrderId = providerOrderId;
    }

    public void setQrReference(String qrReference) {
        this.qrReference = qrReference;
    }
}