package org.example.backend.repository;

import org.example.backend.entity.HealthRiskRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthRiskRecordRepository extends JpaRepository<HealthRiskRecord, Long> {

    /**
     * 根据老人ID查询风险记录(按时间倒序)
     */
    List<HealthRiskRecord> findByElderlyIdOrderByCreatedAtDesc(Long elderlyId);

    /**
     * 查询老人当前(最新)的风险状态
     */
    Optional<HealthRiskRecord> findFirstByElderlyIdOrderByCreatedAtDesc(Long elderlyId);

    /**
     * 查询所有未发送预警的高风险记录
     */
    List<HealthRiskRecord> findByIsAlertedFalseAndRiskLevelIn(List<String> riskLevels);

    /**
     * 根据风险等级查询
     */
    List<HealthRiskRecord> findByRiskLevel(String riskLevel);
}
