package org.example.backend.controller;

import org.example.backend.dto.ApiResponse;
import org.example.backend.dto.HealthDataBatchDTO;
import org.example.backend.entity.*;
import org.example.backend.repository.*;
import org.example.backend.service.HealthDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 健康数据管理Controller
 */
@RestController
@RequestMapping("/api/data")
// ⭐ 修复：删除这个注解，使用WebConfig中的全局配置
// @CrossOrigin(origins = "*")
public class HealthDataController {

    @Autowired
    private HealthDataService healthDataService;

    @Autowired
    private PhysiologicalDataRepository physiologicalDataRepository;

    @Autowired
    private BehaviorDataRepository behaviorDataRepository;

    @Autowired
    private EnvironmentDataRepository environmentDataRepository;

    /**
     * 批量上传多模态健康数据
     */
    @PostMapping("/batch")
    public ApiResponse<Map<String, Object>> uploadBatchData(@RequestBody HealthDataBatchDTO batchDTO) {
        try {
            Map<String, Object> result = healthDataService.saveBatchData(batchDTO);
            return ApiResponse.success("数据上传成功", result);
        } catch (Exception e) {
            return ApiResponse.error("数据上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传生理数据
     */
    @PostMapping("/physiological")
    public ApiResponse<PhysiologicalData> uploadPhysiological(@RequestBody PhysiologicalData data) {
        try {
            PhysiologicalData saved = physiologicalDataRepository.save(data);
            return ApiResponse.success("生理数据上传成功", saved);
        } catch (Exception e) {
            return ApiResponse.error("生理数据上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传行为数据
     */
    @PostMapping("/behavior")
    public ApiResponse<BehaviorData> uploadBehavior(@RequestBody BehaviorData data) {
        try {
            BehaviorData saved = behaviorDataRepository.save(data);
            return ApiResponse.success("行为数据上传成功", saved);
        } catch (Exception e) {
            return ApiResponse.error("行为数据上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传环境数据
     */
    @PostMapping("/environment")
    public ApiResponse<EnvironmentData> uploadEnvironment(@RequestBody EnvironmentData data) {
        try {
            EnvironmentData saved = environmentDataRepository.save(data);
            return ApiResponse.success("环境数据上传成功", saved);
        } catch (Exception e) {
            return ApiResponse.error("环境数据上传失败: " + e.getMessage());
        }
    }

    /**
     * 查询老人最新的健康数据
     */
    @GetMapping("/latest/{elderlyId}")
    public ApiResponse<Map<String, Object>> getLatestData(@PathVariable Long elderlyId) {
        try {
            Map<String, Object> data = healthDataService.getLatestHealthData(elderlyId);
            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询生理数据历史记录
     */
    @GetMapping("/physiological/{elderlyId}")
    public ApiResponse<List<PhysiologicalData>> getPhysiologicalHistory(
            @PathVariable Long elderlyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        try {
            List<PhysiologicalData> data;
            if (startTime != null && endTime != null) {
                data = physiologicalDataRepository.findByElderlyIdAndRecordedAtBetween(elderlyId, startTime, endTime);
            } else {
                data = physiologicalDataRepository.findByElderlyIdOrderByRecordedAtDesc(elderlyId);
            }
            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询最新一条行为数据（供 Unity 轮询使用）
     */
    @GetMapping("/behavior/latest/{elderlyId}")
    public ApiResponse<BehaviorData> getLatestBehavior(@PathVariable Long elderlyId) {
        try {
            List<BehaviorData> list = behaviorDataRepository.findByElderlyIdOrderByRecordedAtDesc(elderlyId);
            if (list.isEmpty()) return ApiResponse.error("暂无行为数据");
            return ApiResponse.success(list.get(0));
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询行为数据历史记录
     */
    @GetMapping("/behavior/{elderlyId}")
    public ApiResponse<List<BehaviorData>> getBehaviorHistory(
            @PathVariable Long elderlyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        try {
            List<BehaviorData> data;
            if (startTime != null && endTime != null) {
                data = behaviorDataRepository.findByElderlyIdAndRecordedAtBetween(elderlyId, startTime, endTime);
            } else {
                data = behaviorDataRepository.findByElderlyIdOrderByRecordedAtDesc(elderlyId);
            }
            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询环境数据历史记录
     */
    @GetMapping("/environment/{elderlyId}")
    public ApiResponse<List<EnvironmentData>> getEnvironmentHistory(
            @PathVariable Long elderlyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        try {
            List<EnvironmentData> data;
            if (startTime != null && endTime != null) {
                data = environmentDataRepository.findByElderlyIdAndRecordedAtBetween(elderlyId, startTime, endTime);
            } else {
                data = environmentDataRepository.findByElderlyIdOrderByRecordedAtDesc(elderlyId);
            }
            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }
}
