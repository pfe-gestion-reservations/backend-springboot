package com.lounes.gestion_reservations.repo;

import com.lounes.gestion_reservations.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FileAttenteRepository extends JpaRepository<FileAttente, Long> {

    List<FileAttente> findByStatutNot(StatutFileAttente statut);

    List<FileAttente> findByEmployeAndStatutNot(Employe employe, StatutFileAttente statut);

    List<FileAttente> findByServiceIdAndHeureDebutAndStatutOrderByHeureArriveeAsc(
            Long serviceId, LocalDateTime heureDebut, StatutFileAttente statut);

    @Query("SELECT fa FROM FileAttente fa WHERE fa.service.id = :serviceId " +
            "AND fa.heureDebut = :heureDebut " +
            "AND fa.statut = com.lounes.gestion_reservations.model.StatutFileAttente.EN_ATTENTE " +
            "ORDER BY fa.heureArrivee ASC")
    Optional<FileAttente> findFirstEnAttenteByServiceAndCreneau(
            @Param("serviceId") Long serviceId,
            @Param("heureDebut") LocalDateTime heureDebut);

    List<FileAttente> findByClientId(Long clientId);

    List<FileAttente> findByEntrepriseIdAndStatutNot(Long entrepriseId, StatutFileAttente statut);

    boolean existsByReservationIdAndStatutNot(Long reservationId, StatutFileAttente statut);

    // ── Suppression en cascade lors du delete d'un service ──
    void deleteByServiceId(Long serviceId);

    // ── Suppression en cascade lors du delete d'une réservation ──
    void deleteByReservationId(Long reservationId);
    List<FileAttente> findByServiceId(Long serviceId);

}