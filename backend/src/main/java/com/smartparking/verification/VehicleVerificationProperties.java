package com.smartparking.verification;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vehicle-verification")
public record VehicleVerificationProperties(
    String provider, String apiUrl, String apiKey, int timeoutSeconds
) {}
