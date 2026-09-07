package com.smartparking.payment.repository;

import com.smartparking.payment.entity.Payment;
import com.smartparking.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBillId(Long billId);

    Optional<Payment> findFirstByBillIdOrderByIdDesc(Long billId);

    Optional<Payment> findByProviderAndProviderPaymentId(
            String provider,
            String providerPaymentId
    );

    Optional<Payment> findByProviderOrderId(
            String providerOrderId
    );

    List<Payment> findByStatus(PaymentStatus status);
}