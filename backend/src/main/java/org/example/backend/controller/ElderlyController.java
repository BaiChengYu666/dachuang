package org.example.backend.controller;

import org.example.backend.dto.ApiResponse;
import org.example.backend.entity.Elderly;
import org.example.backend.repository.ElderlyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 老人信息管理Controller
 */
@RestController
@RequestMapping("/api/elderly")
// ⭐ 修复：删除这个注解，使用WebConfig中的全局配置
// @CrossOrigin(origins = "*")
public class ElderlyController {

    @Autowired
    private ElderlyRepository elderlyRepository;

    /**
     * 新增老人信息
     */
    @PostMapping
    public ApiResponse<Elderly> create(@RequestBody Elderly elderly) {
        try {
            Elderly saved = elderlyRepository.save(elderly);
            return ApiResponse.success("添加成功", saved);
        } catch (Exception e) {
            return ApiResponse.error("添加失败: " + e.getMessage());
        }
    }

    /**
     * 查询所有老人列表
     */
    @GetMapping("/list")
    public ApiResponse<List<Elderly>> list() {
        try {
            List<Elderly> list = elderlyRepository.findAll();
            return ApiResponse.success(list);
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询老人详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Elderly> getById(@PathVariable Long id) {
        try {
            Optional<Elderly> elderly = elderlyRepository.findById(id);
            if (elderly.isPresent()) {
                return ApiResponse.success(elderly.get());
            } else {
                return ApiResponse.error(404, "老人信息不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 更新老人信息
     */
    @PutMapping("/{id}")
    public ApiResponse<Elderly> update(@PathVariable Long id, @RequestBody Elderly elderly) {
        try {
            Optional<Elderly> existingOpt = elderlyRepository.findById(id);
            if (existingOpt.isPresent()) {
                elderly.setId(id);
                Elderly updated = elderlyRepository.save(elderly);
                return ApiResponse.success("更新成功", updated);
            } else {
                return ApiResponse.error(404, "老人信息不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除老人信息
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            if (elderlyRepository.existsById(id)) {
                elderlyRepository.deleteById(id);
                return ApiResponse.success("删除成功", null);
            } else {
                return ApiResponse.error(404, "老人信息不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }
}
