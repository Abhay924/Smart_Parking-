package com.smartparking.gate.simulator;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GateSimulator {

    private final Map<String, Boolean> gateStates = new ConcurrentHashMap<>();

    public void open(String gateId) {
        gateStates.put(gateId, true);
    }

    public void close(String gateId) {
        gateStates.put(gateId, false);
    }

    public boolean isOpen(String gateId) {
        return gateStates.getOrDefault(gateId, false);
    }
}