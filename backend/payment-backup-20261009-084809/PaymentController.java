package com.smartparking.payment.controller;
import com.smartparking.payment.entity.Payment;
import com.smartparking.payment.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestBody CreatePaymentRequest request
    ) {
        Payment payment = paymentService.createPayment(
                request.billId(),
                request.provider()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PaymentResponse.from(payment));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable Long paymentId
    ) {
        Payment payment = paymentService.getPayment(paymentId);

        return ResponseEntity.ok(
                PaymentResponse.from(payment)
        );
    }

    @PostMapping("/{paymentId}/pending")
    public ResponseEntity<PaymentResponse> markPending(
            @PathVariable Long paymentId
    ) {
        Payment payment =
                paymentService.markPaymentPending(paymentId);

        return ResponseEntity.ok(
                PaymentResponse.from(payment)
        );
    }

    @PostMapping("/{paymentId}/success")
    public ResponseEntity<PaymentResponse> markSuccess(
            @PathVariable Long paymentId,
            @RequestParam String providerPaymentId,
            @RequestParam(required = false) String paymentMethod
    ) {
        Payment payment = paymentService.markPaymentSuccess(
                paymentId,
                providerPaymentId,
                paymentMethod
        );

        return ResponseEntity.ok(
                PaymentResponse.from(payment)
        );
    }

    public record CreatePaymentRequest(
            Long billId,
            String provider
    ) {}

    public record PaymentResponse(
            Long id,
            Long billId,
            String provider,
            String providerPaymentId,
            String providerOrderId,
            java.math.BigDecimal amount,
            String currency,
            String status,
            String paymentMethod,
            String qrReference,
            java.time.OffsetDateTime paidAt,
            java.time.OffsetDateTime createdAt
    ) {
        public static PaymentResponse from(Payment payment) {
            return new PaymentResponse(
                    payment.getId(),
                    payment.getBill().getId(),
                    payment.getProvider(),
                    payment.getProviderPaymentId(),
                    payment.getProviderOrderId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getStatus().name(),
                    payment.getPaymentMethod(),
                    payment.getQrReference(),
                    payment.getPaidAt(),
                    payment.getCreatedAt()
            );
        }
    }
}