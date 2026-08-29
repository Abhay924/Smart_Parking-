package com.smartparking.parking.entity;

import com.smartparking.vehicle.entity.Vehicle;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(
    name = "parking_sessions",
    indexes = {
        @Index(name = "idx_parking_session_vehicle", columnList = "vehicle_id"),
        @Index(name = "idx_parking_session_status", columnList = "status")
    }
)
public class ParkingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "vehicle_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_parking_session_vehicle")
    )
    private Vehicle vehicle;

    @Column(name = "entry_time", nullable = false)
    private OffsetDateTime entryTime;

    @Column(name = "exit_time")
    private OffsetDateTime exitTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParkingSessionStatus status;

    protected ParkingSession() {
    }

    public ParkingSession(Vehicle vehicle, OffsetDateTime entryTime) {
        this.vehicle = vehicle;
        this.entryTime = entryTime;
        this.status = ParkingSessionStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public OffsetDateTime getEntryTime() {
        return entryTime;
    }

    public OffsetDateTime getExitTime() {
        return exitTime;
    }

    public ParkingSessionStatus getStatus() {
        return status;
    }

    public void markExited(OffsetDateTime exitTime) {
        this.exitTime = exitTime;
        this.status = ParkingSessionStatus.COMPLETED;
    }
}