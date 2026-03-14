package com.lounes.gestion_reservations.repo;

import com.lounes.gestion_reservations.model.Disponibilite;
import com.lounes.gestion_reservations.model.JourSemaine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;

public interface DisponibiliteRepository extends JpaRepository<Disponibilite, Long> {

    // Toutes les dispos d'un service
    List<Disponibilite> findByServiceId(Long serviceId);

    // Dispos d'un service pour un jour donné
    List<Disponibilite> findByServiceIdAndJour(Long serviceId, JourSemaine jour);

    // Validation : heure dans une plage dispo du service
    List<Disponibilite> findByServiceIdAndJourAndHeureDebutLessThanEqualAndHeureFinGreaterThan(
            Long serviceId,
            JourSemaine jour,
            LocalTime heureDebut,
            LocalTime heureFin
    );

}