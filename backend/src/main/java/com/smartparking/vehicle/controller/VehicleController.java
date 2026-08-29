package com.smartparking.vehicle.controller;

import com.smartparking.vehicle.dto.CreateVehicleRequest;
import com.smartparking.vehicle.entity.Vehicle;
import com.smartparking.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<Vehicle> createVehicle(
            @Valid @RequestBody CreateVehicleRequest request
    ) {
        Vehicle vehicle = vehicleService.createVehicle(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(vehicle);
    }
}