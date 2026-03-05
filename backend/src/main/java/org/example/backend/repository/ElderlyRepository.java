package org.example.backend.repository;

import org.example.backend.entity.Elderly;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ElderlyRepository extends JpaRepository<Elderly, Long> {
}
