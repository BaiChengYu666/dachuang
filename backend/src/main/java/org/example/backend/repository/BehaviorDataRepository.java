package org.example.backend.repository;

import org.example.backend.entity.BehaviorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BehaviorDataRepository extends JpaRepository<BehaviorData, Long> {

    /**
     * 根据老人ID查询行为数据列表(按时间倒序)
     */
    List<BehaviorData> findByElderlyIdOrderByRecordedAtDesc(Long elderlyId);

    /**
     * 查询老人最新的一条行为数据
     */
    Optional<BehaviorData> findFirstByElderlyIdOrderByRecordedAtDesc(Long elderlyId);

    /**
     * 根据老人ID和时间范围查询行为数据
     */
    List<BehaviorData> findByElderlyIdAndRecordedAtBetween(
        Long elderlyId,
        LocalDateTime startTime,
        LocalDateTime endTime
    );

    /**
     * 查询特定活动类型的数据
     */
    List<BehaviorData> findByElderlyIdAndActivityType(Long elderlyId, String activityType);
}
