package org.example.backend.repository;

import org.example.backend.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {
    List<EmergencyContact> findByUserPhoneOrderByIsPrimaryDescCreatedAtAsc(String userPhone);
    void deleteByIdAndUserPhone(Long id, String userPhone);
}
