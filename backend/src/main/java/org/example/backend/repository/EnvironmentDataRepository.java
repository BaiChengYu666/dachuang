package org.example.backend.repository;

import org.example.backend.entity.EnvironmentData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnvironmentDataRepository extends JpaRepository<EnvironmentData, Long> {

    List<EnvironmentData> findByElderlyIdOrderByRecordedAtDesc(Long elderlyId);

    Optional<EnvironmentData> findFirstByElderlyIdOrderByRecordedAtDesc(Long elderlyId);

    List<EnvironmentData> findByElderlyIdAndRecordedAtBetween(
        Long elderlyId,
        LocalDateTime startTime,
        LocalDateTime endTime
    );
}
