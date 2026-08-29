package com.smartparking.parking.service;

import com.smartparking.parking.dto.ParkingEntryRequest;
import com.smartparking.parking.dto.ParkingEntryResponse;
import com.smartparking.parking.entity.ParkingSession;
import com.smartparking.parking.entity.ParkingSessionStatus;
import com.smartparking.parking.repository.ParkingSessionRepository;
import com.smartparking.vehicle.entity.Vehicle;
import com.smartparking.vehicle.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class ParkingService {

    private final VehicleRepository vehicleRepository;
    private final ParkingSessionRepository parkingSessionRepository;

    public ParkingService(
            VehicleRepository vehicleRepository,
            ParkingSessionRepository parkingSessionRepository
    ) {
        this.vehicleRepository = vehicleRepository;
        this.parkingSessionRepository = parkingSessionRepository;
    }

    @Transactional
    public ParkingEntryResponse createEntry(ParkingEntryRequest request) {

        String plateNumber = request.plateNumber()
                .trim()
                .toUpperCase();

        Vehicle vehicle = vehicleRepository
                .findByPlateNumber(plateNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Vehicle not registered: " + plateNumber
                        )
                );

        parkingSessionRepository
                .findByVehicleIdAndStatus(
                        vehicle.getId(),
                        ParkingSessionStatus.ACTIVE
                )
                .ifPresent(session -> {
                    throw new IllegalStateException(
                            "Vehicle already has an active parking session"
                    );
                });

        ParkingSession session = new ParkingSession(
                vehicle,
                OffsetDateTime.now()
        );

        ParkingSession saved =
                parkingSessionRepository.save(session);

        return new ParkingEntryResponse(
                saved.getId(),
                vehicle.getId(),
                vehicle.getPlateNumber(),
                saved.getEntryTime(),
                saved.getStatus().name()
        );
    }
}