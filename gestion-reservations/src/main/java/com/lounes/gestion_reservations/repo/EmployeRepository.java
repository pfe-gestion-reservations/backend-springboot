package com.lounes.gestion_reservations.repo;

import com.lounes.gestion_reservations.model.Employe;
import com.lounes.gestion_reservations.model.Entreprise;
import com.lounes.gestion_reservations.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeRepository extends JpaRepository<Employe, Long> {
    Optional<Employe> findByUser(User user);
    Optional<Employe> findByUserId(Long userId);
    List<Employe> findByEntreprise(Entreprise entreprise);
    // Employé actif (non archivé) pour un user donné
    Optional<Employe> findByUserIdAndArchivedFalse(Long userId);
    // Tous les profils employé d'un user (peut en avoir plusieurs dans différentes entreprises)
    List<Employe> findAllByUserId(Long userId);
}