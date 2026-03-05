package org.example.backend.repository;

import org.example.backend.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByUserPhone(String userPhone);
    void deleteByIdAndUserPhone(Long id, String userPhone);
}
