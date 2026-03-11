package com.lounes.gestion_reservations.repo;

import com.lounes.gestion_reservations.model.Ressource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RessourceRepository extends JpaRepository<Ressource, Long> {

    List<Ressource> findByServiceId(Long serviceId);
    List<Ressource> findByEntrepriseId(Long entrepriseId);
    List<Ressource> findByServiceIdAndArchivedFalse(Long serviceId);
    boolean existsByNomIgnoreCaseAndServiceId(String nom, Long serviceId);
}