package com.lounes.gestion_reservations.repo;

import com.lounes.gestion_reservations.model.Disponibilite;
import com.lounes.gestion_reservations.model.JourSemaine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;

public interface DisponibiliteRepository extends JpaRepository<Disponibilite, Long> {

    List<Disponibilite> findByServiceId(Long serviceId);

    List<Disponibilite> findByServiceIdAndJour(Long serviceId, JourSemaine jour);

    List<Disponibilite> findByServiceIdAndJourAndHeureDebutLessThanEqualAndHeureFinGreaterThan(
            Long serviceId,
            JourSemaine jour,
            LocalTime heureDebut,
            LocalTime heureFin
    );

}