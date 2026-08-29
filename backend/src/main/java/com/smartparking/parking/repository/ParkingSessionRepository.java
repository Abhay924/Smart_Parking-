package com.smartparking.parking.repository;

import com.smartparking.parking.entity.ParkingSession;
import com.smartparking.parking.entity.ParkingSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParkingSessionRepository
        extends JpaRepository<ParkingSession, Long> {

    Optional<ParkingSession> findByVehicleIdAndStatus(
            Long vehicleId,
            ParkingSessionStatus status
    );
}