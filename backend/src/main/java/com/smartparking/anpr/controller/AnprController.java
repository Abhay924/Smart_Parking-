package com.smartparking.anpr.controller;

import com.smartparking.anpr.dto.AnprDetectionRequest;
import com.smartparking.anpr.dto.AnprDetectionResponse;
import com.smartparking.anpr.service.AnprService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/anpr")
public class AnprController {

    private final AnprService anprService;

    public AnprController(AnprService anprService) {
        this.anprService = anprService;
    }

    @PostMapping("/detect")
    public ResponseEntity<AnprDetectionResponse> detect(
            @Valid @RequestBody AnprDetectionRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(anprService.processDetection(request));
    }
}