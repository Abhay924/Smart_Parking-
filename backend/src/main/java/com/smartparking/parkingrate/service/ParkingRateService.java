package com.smartparking.parkingrate.service;

import com.smartparking.parkingrate.dto.ParkingRateConfigRequest;
import com.smartparking.parkingrate.dto.ParkingRateResponse;
import com.smartparking.parkingrate.entity.ParkingRate;
import com.smartparking.parkingrate.repository.ParkingRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ParkingRateService {

    private final ParkingRateRepository parkingRateRepository;

    public ParkingRateService(
            ParkingRateRepository parkingRateRepository
    ) {
        this.parkingRateRepository = parkingRateRepository;
    }

    @Transactional(readOnly = true)
    public List<ParkingRateResponse> getAllRates() {

        return parkingRateRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ParkingRateResponse getActiveRate() {

        ParkingRate rate = parkingRateRepository
                .findFirstByVehicleTypeAndActiveTrueOrderByIdDesc("CAR")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No active parking rate configured"
                        )
                );

        return toResponse(rate);
    }

    @Transactional
    public ParkingRateResponse updateRate(
            Long id,
            ParkingRateConfigRequest request
    ) {

        ParkingRate rate = parkingRateRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parking rate not found: " + id
                        )
                );

        rate.updateConfiguration(
                request.name().trim(),
                request.billingMode(),
                request.ratePerUnit(),
                request.minimumCharge(),
                request.gracePeriodMinutes(),
                request.maximumDailyCharge()
        );

        ParkingRate saved =
                parkingRateRepository.save(rate);

        return toResponse(saved);
    }

    @Transactional
    public ParkingRateResponse createRate(
            ParkingRateConfigRequest request
    ) {

        // Deactivate all currently active rates.
        parkingRateRepository.findByActiveTrue()
                .forEach(rate -> rate.deactivate());

        ParkingRate rate = new ParkingRate();

        rate.updateConfiguration(
                request.name().trim(),
                request.billingMode(),
                request.ratePerUnit(),
                request.minimumCharge(),
                request.gracePeriodMinutes(),
                request.maximumDailyCharge()
        );

        rate.activate();

        ParkingRate saved =
                parkingRateRepository.save(rate);

        return toResponse(saved);
    }

    private ParkingRateResponse toResponse(ParkingRate rate) {

        return new ParkingRateResponse(
                rate.getId(),
                rate.getName(),
                rate.getVehicleType(),
                rate.getBillingMode(),
                rate.getRatePerUnit(),
                rate.getMinimumCharge(),
                rate.getGracePeriodMinutes(),
                rate.getMaximumDailyCharge(),
                rate.getActive(),
                rate.getCreatedAt()
        );
    }
}