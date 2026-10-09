package com.smartparking.anpr.repository;

import com.smartparking.anpr.entity.AnprDetection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnprDetectionRepository
        extends JpaRepository<AnprDetection, Long> {
}