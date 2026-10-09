package com.smartparking.verification;

public interface VehicleVerificationProvider {
    ProviderResult verify(String plateNumber);
    record ProviderResult(boolean found, String providerReference, String message) {}
}
