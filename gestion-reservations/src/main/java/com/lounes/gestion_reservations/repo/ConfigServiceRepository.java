package com.lounes.gestion_reservations.repo;

import com.lounes.gestion_reservations.model.ConfigService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigServiceRepository extends JpaRepository<ConfigService, Long> {

    Optional<ConfigService> findByServiceId(Long serviceId);
    boolean existsByServiceId(Long serviceId);
}