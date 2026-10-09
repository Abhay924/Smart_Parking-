
package com.smartparking.anpr.service;

import com.smartparking.anpr.entity.AnprDetection;
import com.smartparking.anpr.repository.AnprDetectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnprEventLogService {

    private final AnprDetectionRepository repository;

    public AnprEventLogService(
            AnprDetectionRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long recordDetection(
            String plateNumber,
            Double confidence,
            String cameraId,
            String direction,
            String imagePath) {

        AnprDetection event = AnprDetection.create(
                plateNumber,
                confidence,
                cameraId,
                direction,
                imagePath
        );

        AnprDetection saved = repository.saveAndFlush(event);
        return saved.getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAccepted(
            Long eventId,
            long processingTimeMs) {

        AnprDetection event = getEvent(eventId);

        event.setAccepted(true);
        event.setRejectionReason(null);
        event.setProcessingTimeMs(toInt(processingTimeMs));

        repository.saveAndFlush(event);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRejected(
            Long eventId,
            String reason,
            long processingTimeMs) {

        AnprDetection event = getEvent(eventId);

        event.setAccepted(false);
        event.setRejectionReason(truncate(reason, 255));
        event.setProcessingTimeMs(toInt(processingTimeMs));

        repository.saveAndFlush(event);
    }

    private AnprDetection getEvent(Long eventId) {
        return repository.findById(eventId)
                .orElseThrow(() -> new IllegalStateException(
                        "ANPR audit event not found: " + eventId
                ));
    }

    private int toInt(long duration) {
        return (int) Math.min(
                Math.max(duration, 0),
                Integer.MAX_VALUE
        );
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "Processing failed";
        }

        return value.length() <= maxLength
                ? value
                : value.substring(0, maxLength);
    }
}