package com.smartparking.parkingrate.repository;

import com.smartparking.parkingrate.entity.ParkingRate;
import com.smartparking.parkingrate.entity.BillingMode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParkingRateRepository
        extends JpaRepository<ParkingRate, Long> {

    List<ParkingRate> findByActiveTrue();

    Optional<ParkingRate> findFirstByVehicleTypeAndActiveTrueOrderByIdDesc(
            String vehicleType
    );
}