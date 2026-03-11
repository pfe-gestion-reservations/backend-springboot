package com.lounes.gestion_reservations.repo;

import com.lounes.gestion_reservations.model.Avis;
import com.lounes.gestion_reservations.model.Employe;
import com.lounes.gestion_reservations.model.Reservation;
import com.lounes.gestion_reservations.model.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AvisRepository extends JpaRepository<Avis, Long> {
    boolean existsByReservation(Reservation reservation);
    List<Avis> findByEmploye(Employe employe);
    List<Avis> findByService(ServiceEntity service);

    @Query("SELECT a FROM Avis a WHERE a.reservation.entreprise.id = :entrepriseId")
    List<Avis> findByEntrepriseId(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT a FROM Avis a WHERE a.reservation.client.id = :clientId")
    List<Avis> findByClientId(@Param("clientId") Long clientId);
}