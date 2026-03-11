package com.lounes.gestion_reservations.repo;

import com.lounes.gestion_reservations.model.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EntrepriseRepository extends JpaRepository<Entreprise, Long> {
    Optional<Entreprise> findByGerantId(Long gerantId);
    List<Entreprise> findBySecteurId(Long secteurId);
    Boolean existsByNom(String nom);
    Boolean existsByTelephone(String telephone);
    Boolean existsByTelephoneAndIdNot(String telephone, Long id);

    @Query("SELECT e FROM Entreprise e JOIN Employe emp ON emp.entreprise = e WHERE emp.user.id = :userId")
    Optional<Entreprise> findByEmployeUserId(@Param("userId") Long userId);
}