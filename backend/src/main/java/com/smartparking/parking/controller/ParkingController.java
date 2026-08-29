package com.smartparking.parking.controller;

import com.smartparking.parking.dto.ParkingEntryRequest;
import com.smartparking.parking.dto.ParkingEntryResponse;
import com.smartparking.parking.service.ParkingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parking")
public class ParkingController {

    private final ParkingService parkingService;

    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @PostMapping("/entry")
    @ResponseStatus(HttpStatus.CREATED)
    public ParkingEntryResponse createEntry(
            @Valid @RequestBody ParkingEntryRequest request
    ) {
        return parkingService.createEntry(request);
    }
}