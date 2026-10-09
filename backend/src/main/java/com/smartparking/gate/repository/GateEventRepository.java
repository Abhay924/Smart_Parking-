package com.smartparking.gate.repository;

import com.smartparking.gate.entity.GateEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GateEventRepository extends JpaRepository<GateEvent, Long> {

    List<GateEvent> findByGateIdOrderByCreatedAtDesc(String gateId);

    List<GateEvent> findByCorrelationId(UUID correlationId);

    List<GateEvent> findByParkingSessionIdOrderByCreatedAtDesc(Long parkingSessionId);

    List<GateEvent> findByBillIdOrderByCreatedAtDesc(Long billId);
}