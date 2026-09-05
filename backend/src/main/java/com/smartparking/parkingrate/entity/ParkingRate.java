package com.smartparking.parkingrate.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "parking_rates")
public class ParkingRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "vehicle_type", nullable = false, length = 20)
    private String vehicleType = "CAR";

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_mode", nullable = false, length = 20)
    private BillingMode billingMode = BillingMode.PER_HOUR;

    @Column(name = "rate_per_unit", nullable = false, precision = 10, scale = 2)
    private BigDecimal ratePerUnit;

    @Column(name = "rate_per_hour", precision = 10, scale = 2)
    private BigDecimal ratePerHour;

    @Column(name = "minimum_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal minimumCharge = BigDecimal.ZERO;

    @Column(name = "grace_period_minutes", nullable = false)
    private Integer gracePeriodMinutes = 0;

    @Column(name = "maximum_daily_charge", precision = 10, scale = 2)
    private BigDecimal maximumDailyCharge;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public ParkingRate() {
    }

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public BillingMode getBillingMode() {
        return billingMode;
    }

    public BigDecimal getRatePerUnit() {
        return ratePerUnit;
    }

    public BigDecimal getRatePerHour() {
        return ratePerHour;
    }

    public BigDecimal getMinimumCharge() {
        return minimumCharge;
    }

    public Integer getGracePeriodMinutes() {
        return gracePeriodMinutes;
    }

    public BigDecimal getMaximumDailyCharge() {
        return maximumDailyCharge;
    }

    public Boolean getActive() {
        return active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void updateConfiguration(
            String name,
            BillingMode billingMode,
            BigDecimal ratePerUnit,
            BigDecimal minimumCharge,
            Integer gracePeriodMinutes,
            BigDecimal maximumDailyCharge
    ) {
        this.name = name;
        this.billingMode = billingMode;
        this.ratePerUnit = ratePerUnit;
        this.minimumCharge = minimumCharge;
        this.gracePeriodMinutes = gracePeriodMinutes;
        this.maximumDailyCharge = maximumDailyCharge;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}