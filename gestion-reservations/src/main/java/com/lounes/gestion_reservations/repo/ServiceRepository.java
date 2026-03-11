package com.lounes.gestion_reservations.repo;

import com.lounes.gestion_reservations.model.Entreprise;
import com.lounes.gestion_reservations.model.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    List<ServiceEntity> findByEntreprise(Entreprise entreprise);
    Optional<ServiceEntity> findByNomIgnoreCaseAndEntreprise(String nom, Entreprise entreprise);
    List<ServiceEntity> findAllByNomIgnoreCaseAndEntreprise(String nom, Entreprise entreprise);
}