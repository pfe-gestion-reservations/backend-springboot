package com.lounes.gestion_reservations.repo;

import com.lounes.gestion_reservations.model.FileAttente;
import com.lounes.gestion_reservations.model.Reservation;
import com.lounes.gestion_reservations.model.StatutFileAttente;
import com.lounes.gestion_reservations.model.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByEntrepriseId(Long entrepriseId);
    List<Reservation> findByClientId(Long clientId);
    List<Reservation> findByEmployeId(Long employeId);
    List<Reservation> findByEmployeIdAndStatut(Long employeId, StatutReservation statut);

    boolean existsByServiceIdAndHeureDebutAndStatutNot(
            Long serviceId, LocalDateTime heureDebut, StatutReservation statut);

    List<Reservation> findByServiceIdAndHeureDebutBetween(
            Long serviceId, LocalDateTime debut, LocalDateTime fin);

    List<Reservation> findByRessourceIdAndHeureDebutBetween(
            Long ressourceId, LocalDateTime debut, LocalDateTime fin);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.ressource.id = :ressourceId " +
            "AND r.statut <> com.lounes.gestion_reservations.model.StatutReservation.ANNULEE " +
            "AND r.heureDebut < :heureFin AND r.heureFin > :heureDebut " +
            "AND (:excludeId IS NULL OR r.id <> :excludeId)")
    boolean isRessourceOccupee(@Param("ressourceId") Long ressourceId,
                               @Param("heureDebut") LocalDateTime heureDebut,
                               @Param("heureFin") LocalDateTime heureFin,
                               @Param("excludeId") Long excludeId);

    void deleteByServiceId(Long serviceId);
    List<Reservation> findByServiceId(Long serviceId);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.client.id = :clientId " +
            "AND r.service.id = :serviceId " +
            "AND r.statut <> com.lounes.gestion_reservations.model.StatutReservation.ANNULEE " +
            "AND r.heureDebut < :heureFin AND r.heureFin > :heureDebut " +
            "AND (:excludeId IS NULL OR r.id <> :excludeId)")
    boolean hasClientOverlap(@Param("clientId") Long clientId,
                             @Param("serviceId") Long serviceId,
                             @Param("heureDebut") LocalDateTime heureDebut,
                             @Param("heureFin") LocalDateTime heureFin,
                             @Param("excludeId") Long excludeId);


    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.service.id = :serviceId " +
            "AND r.statut <> com.lounes.gestion_reservations.model.StatutReservation.ANNULEE " +
            "AND r.heureDebut < :heureFin AND r.heureFin > :heureDebut " +
            "AND (:excludeId IS NULL OR r.id <> :excludeId)")
    boolean hasServiceOverlap(@Param("serviceId") Long serviceId,
                              @Param("heureDebut") LocalDateTime heureDebut,
                              @Param("heureFin") LocalDateTime heureFin,
                              @Param("excludeId") Long excludeId);

    List<Reservation> findByClientIdAndStatutNot(Long clientId, StatutReservation statut);


}