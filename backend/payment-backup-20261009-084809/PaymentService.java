package com.smartparking.payment.service;
import com.smartparking.billing.entity.Bill;
import com.smartparking.billing.repository.BillRepository;
import com.smartparking.payment.entity.Payment;
import com.smartparking.payment.entity.PaymentStatus;
import com.smartparking.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            BillRepository billRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.billRepository = billRepository;
    }

    @Transactional
    public Payment createPayment(Long billId, String provider) {

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Bill not found: " + billId)
                );

        if (bill.getStatus() != com.smartparking.billing.entity.BillStatus.UNPAID) {
            throw new IllegalStateException(
                    "Payment cannot be created for bill with status: "
                            + bill.getStatus()
            );
        }

        Payment existingPayment =
                paymentRepository
                        .findFirstByBillIdOrderByIdDesc(billId)
                        .orElse(null);

        if (existingPayment != null &&
                (existingPayment.getStatus() == PaymentStatus.CREATED ||
                 existingPayment.getStatus() == PaymentStatus.PENDING)) {

            return existingPayment;
        }
        BigDecimal amount = bill.getTotalAmount();

        if (amount == null || amount.signum() < 0) {
            throw new IllegalStateException(
                    "Invalid bill amount: " + amount
            );
        }

        Payment payment = new Payment(
                bill,
                provider,
                amount
        );

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment markPaymentPending(Long paymentId) {

        Payment payment = getPayment(paymentId);

        if (payment.getStatus() != PaymentStatus.CREATED) {
            throw new IllegalStateException(
                    "Payment cannot be moved to PENDING from status: "
                            + payment.getStatus()
            );
        }

        payment.markPending();

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment markPaymentSuccess(
            Long paymentId,
            String providerPaymentId,
            String paymentMethod
    ) {

        Payment payment = getPayment(paymentId);

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return payment;
        }

        if (payment.getStatus() != PaymentStatus.PENDING &&
                payment.getStatus() != PaymentStatus.CREATED) {

            throw new IllegalStateException(
                    "Payment cannot be marked SUCCESS from status: "
                            + payment.getStatus()
            );
        }

        if (providerPaymentId == null ||
                providerPaymentId.isBlank()) {

            throw new IllegalArgumentException(
                    "Provider payment ID is required"
            );
        }

        payment.markSuccess(
                providerPaymentId,
                paymentMethod
        );

        Bill bill = payment.getBill();

        if (bill.getStatus() != com.smartparking.billing.entity.BillStatus.UNPAID) {
            throw new IllegalStateException(
                    "Bill is not UNPAID: " + bill.getStatus()
            );
        }

        bill.markPaid();

        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Payment getPayment(Long paymentId) {

        return paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment not found: " + paymentId
                        )
                );
    }
}