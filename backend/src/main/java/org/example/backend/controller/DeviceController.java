package org.example.backend.controller;

import org.example.backend.dto.ApiResponse;
import org.example.backend.entity.Device;
import org.example.backend.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/device")
public class DeviceController {

    @Autowired
    private DeviceRepository deviceRepository;

    /** 查询用户的所有设备 */
    @GetMapping("/{userPhone}")
    public ApiResponse<List<Device>> listDevices(@PathVariable String userPhone) {
        return ApiResponse.success(deviceRepository.findByUserPhone(userPhone));
    }

    /** 添加设备 */
    @PostMapping
    public ApiResponse<Device> addDevice(@RequestBody Device device) {
        if (device.getUserPhone() == null || device.getDeviceName() == null) {
            return ApiResponse.error("用户手机号和设备名称不能为空");
        }
        Device saved = deviceRepository.save(device);
        return ApiResponse.success("设备添加成功", saved);
    }

    /** 更新设备状态 */
    @PutMapping("/{id}")
    public ApiResponse<Device> updateDevice(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return deviceRepository.findById(id).map(d -> {
            if (body.containsKey("deviceName")) d.setDeviceName(body.get("deviceName"));
            if (body.containsKey("status"))     d.setStatus(body.get("status"));
            return ApiResponse.success("更新成功", deviceRepository.save(d));
        }).orElse(ApiResponse.error("设备不存在"));
    }

    /** 删除设备 */
    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> deleteDevice(
            @PathVariable Long id,
            @RequestParam String userPhone) {
        if (!deviceRepository.existsById(id)) {
            return ApiResponse.error("设备不存在");
        }
        deviceRepository.deleteByIdAndUserPhone(id, userPhone);
        return ApiResponse.success("删除成功", null);
    }
}
