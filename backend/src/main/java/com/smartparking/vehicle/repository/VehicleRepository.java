package com.smartparking.vehicle.repository;

import com.smartparking.vehicle.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByPlateNumber(String plateNumber);

    boolean existsByPlateNumber(String plateNumber);
}