package com.smartparking.gate.controller;

import com.smartparking.gate.dto.GateCommandRequest;
import com.smartparking.gate.dto.GateResponse;
import com.smartparking.gate.entity.GateDirection;
import com.smartparking.gate.service.GateService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gates")
public class GateController {

    private final GateService gateService;

    public GateController(GateService gateService) {
        this.gateService = gateService;
    }

    @PostMapping("/open")
    public ResponseEntity<GateResponse> openGate(
            @Valid @RequestBody GateCommandRequest request
    ) {

        return ResponseEntity.ok(
                GateResponse.from(
                        gateService.openGate(request)
                )
        );
    }

    @PostMapping("/{gateId}/close")
    public ResponseEntity<GateResponse> closeGate(
            @PathVariable String gateId,
            @RequestParam GateDirection direction,
            @RequestParam Long sessionId,
            @RequestParam(required = false) Long billId
    ) {

        return ResponseEntity.ok(
                GateResponse.from(
                        gateService.closeGate(
                                gateId,
                                direction,
                                sessionId,
                                billId
                        )
                )
        );
    }

    @PostMapping("/{gateId}/manual-open")
    public ResponseEntity<GateResponse> manualOpen(
            @PathVariable String gateId,
            @RequestParam GateDirection direction,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long billId,
            @RequestParam String reason
    ) {

        return ResponseEntity.ok(
                GateResponse.from(
                        gateService.manualOpenGate(
                                gateId,
                                direction,
                                sessionId,
                                billId,
                                reason
                        )
                )
        );
    }

    @PostMapping("/{gateId}/emergency-open")
    public ResponseEntity<GateResponse> emergencyOpen(
            @PathVariable String gateId,
            @RequestParam GateDirection direction,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long billId,
            @RequestParam String reason
    ) {

        return ResponseEntity.ok(
                GateResponse.from(
                        gateService.emergencyOpenGate(
                                gateId,
                                direction,
                                sessionId,
                                billId,
                                reason
                        )
                )
        );
    }

    @GetMapping("/{gateId}/status")
    public ResponseEntity<Boolean> gateStatus(
            @PathVariable String gateId
    ) {

        return ResponseEntity.ok(
                gateService.isGateOpen(gateId)
        );
    }
}