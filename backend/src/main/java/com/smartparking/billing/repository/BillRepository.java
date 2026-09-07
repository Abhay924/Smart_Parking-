package com.smartparking.billing.repository;

import com.smartparking.billing.entity.Bill;
import com.smartparking.billing.entity.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findBySessionId(Long sessionId);

    Optional<Bill> findBySessionIdAndStatus(
            Long sessionId,
            BillStatus status
    );
}