package com.smartparking.vehicle.service;

import com.smartparking.vehicle.dto.CreateVehicleRequest;
import com.smartparking.vehicle.entity.Vehicle;
import com.smartparking.vehicle.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public Vehicle createVehicle(CreateVehicleRequest request) {

        String plateNumber = normalizePlateNumber(request.plateNumber());

        if (vehicleRepository.existsByPlateNumber(plateNumber)) {
            throw new IllegalArgumentException(
                    "Vehicle with plate number already exists"
            );
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setPlateNumber(plateNumber);

        return vehicleRepository.save(vehicle);
    }

    private String normalizePlateNumber(String plateNumber) {
        return plateNumber
                .trim()
                .replaceAll("\\s+", "")
                .toUpperCase(Locale.ROOT);
    }
}