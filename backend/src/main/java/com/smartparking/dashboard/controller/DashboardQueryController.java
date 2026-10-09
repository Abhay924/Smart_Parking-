
package com.smartparking.dashboard.controller;

import com.smartparking.billing.dto.BillResponse;
import com.smartparking.billing.entity.Bill;
import com.smartparking.billing.entity.BillStatus;
import com.smartparking.billing.repository.BillRepository;
import com.smartparking.parking.entity.ParkingSession;
import com.smartparking.parking.entity.ParkingSessionStatus;
import com.smartparking.parking.repository.ParkingSessionRepository;
import com.smartparking.vehicle.entity.Vehicle;
import com.smartparking.vehicle.repository.VehicleRepository;

import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@Transactional(readOnly = true)
public class DashboardQueryController {

    private final VehicleRepository vehicleRepository;
    private final ParkingSessionRepository sessionRepository;
    private final BillRepository billRepository;

    public DashboardQueryController(
            VehicleRepository vehicleRepository,
            ParkingSessionRepository sessionRepository,
            BillRepository billRepository) {
        this.vehicleRepository = vehicleRepository;
        this.sessionRepository = sessionRepository;
        this.billRepository = billRepository;
    }

    @GetMapping("/vehicles")
    public List<VehicleItem> getVehicles() {
        return vehicleRepository
                .findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(v -> new VehicleItem(
                        v.getId(),
                        v.getPlateNumber(),
                        v.getVehicleType().name(),
                        v.isActive(),
                        v.getCreatedAt()))
                .toList();
    }

    @GetMapping("/sessions")
    public List<SessionItem> getSessions() {
        OffsetDateTime now = OffsetDateTime.now();

        return sessionRepository
                .findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(s -> new SessionItem(
                        s.getId(),
                        s.getVehicle().getId(),
                        s.getVehicle().getPlateNumber(),
                        s.getEntryTime(),
                        s.getExitTime(),
                        s.getStatus().name(),
                        Math.max(0, java.time.Duration.between(
                                s.getEntryTime(),
                                s.getExitTime() != null
                                        ? s.getExitTime()
                                        : now
                        ).toMinutes())))
                .toList();
    }

    @GetMapping("/bills")
    public List<BillResponse> getBills() {
        return billRepository
                .findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(BillResponse::from)
                .toList();
    }

    @GetMapping("/stats")
    public DashboardStats getStats() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        List<ParkingSession> sessions = sessionRepository.findAll();
        List<Bill> bills = billRepository.findAll();

        long activeSessions = sessions.stream()
                .filter(s -> s.getStatus() == ParkingSessionStatus.ACTIVE)
                .count();

        long paidBillsToday = bills.stream()
                .filter(b -> b.getStatus() == BillStatus.PAID)
                .filter(b -> b.getPaidAt() != null)
                .filter(b -> b.getPaidAt().toLocalDate()
                        .equals(LocalDate.now()))
                .count();

        BigDecimal revenueToday = bills.stream()
                .filter(b -> b.getStatus() == BillStatus.PAID)
                .filter(b -> b.getPaidAt() != null)
                .filter(b -> b.getPaidAt().toLocalDate()
                        .equals(LocalDate.now()))
                .map(Bill::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardStats(
                vehicles.size(),
                activeSessions,
                bills.size(),
                paidBillsToday,
                revenueToday,
                null
        );
    }

    public record VehicleItem(
            Long id,
            String plateNumber,
            String vehicleType,
            boolean active,
            OffsetDateTime createdAt) {
    }

    public record SessionItem(
            Long id,
            Long vehicleId,
            String plateNumber,
            OffsetDateTime entryTime,
            OffsetDateTime exitTime,
            String status,
            long durationMinutes) {
    }

    public record DashboardStats(
            long registeredVehicles,
            long currentlyParked,
            long totalBills,
            long paidBillsToday,
            BigDecimal revenueToday,
            Integer totalCapacity) {
    }
}
