package com.smartparking.billing.controller;

import com.smartparking.billing.dto.BillResponse;
import com.smartparking.billing.service.BillingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/billing")
public class BillingController {

    private final BillingService billingService;

    public BillingController(
            BillingService billingService
    ) {
        this.billingService = billingService;
    }

    @PostMapping("/session/{sessionId}")
    @ResponseStatus(HttpStatus.CREATED)
    public BillResponse generateBill(
            @PathVariable Long sessionId
    ) {
        return billingService
                .generateBillResponse(sessionId);
    }
}