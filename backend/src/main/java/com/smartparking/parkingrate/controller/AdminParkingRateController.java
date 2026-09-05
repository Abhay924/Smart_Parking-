package com.smartparking.parkingrate.controller;

import com.smartparking.parkingrate.dto.ParkingRateConfigRequest;
import com.smartparking.parkingrate.dto.ParkingRateResponse;
import com.smartparking.parkingrate.service.ParkingRateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/parking-rates")
public class AdminParkingRateController {

    private final ParkingRateService parkingRateService;

    public AdminParkingRateController(
            ParkingRateService parkingRateService
    ) {
        this.parkingRateService = parkingRateService;
    }

    @GetMapping
    public List<ParkingRateResponse> getAllRates() {
        return parkingRateService.getAllRates();
    }

    @GetMapping("/active")
    public ParkingRateResponse getActiveRate() {
        return parkingRateService.getActiveRate();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParkingRateResponse createRate(
            @Valid @RequestBody ParkingRateConfigRequest request
    ) {
        return parkingRateService.createRate(request);
    }

    @PutMapping("/{id}")
    public ParkingRateResponse updateRate(
            @PathVariable Long id,
            @Valid @RequestBody ParkingRateConfigRequest request
    ) {
        return parkingRateService.updateRate(id, request);
    }
}