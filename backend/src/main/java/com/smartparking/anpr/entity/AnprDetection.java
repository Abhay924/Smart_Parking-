
package com.smartparking.anpr.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "anpr_events",
    indexes = {
        @Index(name = "idx_anpr_plate", columnList = "plate_number"),
        @Index(name = "idx_anpr_camera", columnList = "camera_id"),
        @Index(name = "idx_anpr_recognized_at", columnList = "recognized_at")
    }
)
public class AnprDetection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plate_number", length = 20)
    private String plateNumber;

    @Column(name = "confidence", precision = 5, scale = 4)
    private BigDecimal confidence;

    @Column(name = "camera_id", nullable = false, length = 100)
    private String cameraId;

    @Column(name = "direction", nullable = false, length = 10)
    private String direction;

    @Column(name = "image_path", columnDefinition = "TEXT")
    private String imagePath;

    @Column(name = "recognized_at", nullable = false)
    private OffsetDateTime recognizedAt;

    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;

    @Column(name = "is_accepted", nullable = false)
    private boolean accepted = false;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    protected AnprDetection() {
    }

    public static AnprDetection create(
            String plateNumber,
            Double confidence,
            String cameraId,
            String direction,
            String imagePath) {

        AnprDetection event = new AnprDetection();

        event.setPlateNumber(plateNumber);
        event.setConfidence(
            confidence == null ? null : BigDecimal.valueOf(confidence)
        );
        event.setCameraId(cameraId);
        event.setDirection(direction);
        event.setImagePath(imagePath);
        event.setAccepted(false);

        return event;
    }

    @PrePersist
    protected void onCreate() {
        if (recognizedAt == null) {
            recognizedAt = OffsetDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public OffsetDateTime getRecognizedAt() {
        return recognizedAt;
    }

    public void setRecognizedAt(OffsetDateTime recognizedAt) {
        this.recognizedAt = recognizedAt;
    }

    public Integer getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Integer processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
