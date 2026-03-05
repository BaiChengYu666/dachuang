package org.example.backend.repository;

import org.example.backend.entity.PhysiologicalData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhysiologicalDataRepository extends JpaRepository<PhysiologicalData, Long> {

    /**
     * 根据老人ID查询生理数据列表(按时间倒序)
     */
    List<PhysiologicalData> findByElderlyIdOrderByRecordedAtDesc(Long elderlyId);

    /**
     * 查询老人最新的一条生理数据
     */
    Optional<PhysiologicalData> findFirstByElderlyIdOrderByRecordedAtDesc(Long elderlyId);

    /**
     * 根据老人ID和时间范围查询生理数据
     */
    List<PhysiologicalData> findByElderlyIdAndRecordedAtBetween(
        Long elderlyId, 
        LocalDateTime startTime, 
        LocalDateTime endTime
    );

    /**
     * 统计某段时间内的平均心率
     */
    @Query("SELECT AVG(p.heartRate) FROM PhysiologicalData p " +
           "WHERE p.elderlyId = :elderlyId AND p.recordedAt BETWEEN :startTime AND :endTime")
    Double calculateAverageHeartRate(
        @Param("elderlyId") Long elderlyId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
