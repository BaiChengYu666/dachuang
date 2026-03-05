package org.example.backend.repository;

import org.example.backend.entity.AlertRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRecordRepository extends JpaRepository<AlertRecord, Long> {

    /**
     * 根据老人ID查询预警记录
     */
    List<AlertRecord> findByElderlyIdOrderByCreatedAtDesc(Long elderlyId);

    /**
     * 查询所有未处理的预警
     */
    List<AlertRecord> findByIsHandledFalseOrderByCreatedAtDesc();

    /**
     * 查询老人未处理的预警
     */
    List<AlertRecord> findByElderlyIdAndIsHandledFalseOrderByCreatedAtDesc(Long elderlyId);

    /**
     * 根据预警级别查询
     */
    List<AlertRecord> findByAlertLevelOrderByCreatedAtDesc(String alertLevel);
}
