package com.lounes.gestion_reservations.repo;

import com.lounes.gestion_reservations.model.Secteur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecteurRepository extends JpaRepository<Secteur, Long> {
    Boolean existsByNom(String nom);
}