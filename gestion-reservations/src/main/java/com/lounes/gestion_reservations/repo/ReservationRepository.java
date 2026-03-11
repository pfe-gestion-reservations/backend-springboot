package com.lounes.gestion_reservations.repo;

import com.lounes.gestion_reservations.model.Reservation;
import com.lounes.gestion_reservations.model.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByEntrepriseId(Long entrepriseId);
    List<Reservation> findByClientId(Long clientId);
    List<Reservation> findByEmployeId(Long employeId);
    List<Reservation> findByEmployeIdAndStatut(Long employeId, StatutReservation statut);

    // Vérifie conflit : même service + chevauchement de créneau
    boolean existsByServiceIdAndHeureDebutAndStatutNot(
            Long serviceId, LocalDateTime heureDebut, StatutReservation statut);

    // Créneaux occupés pour un service sur une période
    List<Reservation> findByServiceIdAndHeureDebutBetween(
            Long serviceId, LocalDateTime debut, LocalDateTime fin);

    // Créneaux occupés pour une ressource (terrain, box...)
    List<Reservation> findByRessourceIdAndHeureDebutBetween(
            Long ressourceId, LocalDateTime debut, LocalDateTime fin);
}