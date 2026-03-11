package com.lounes.gestion_reservations.repo;

import com.lounes.gestion_reservations.model.Employe;
import com.lounes.gestion_reservations.model.FileAttente;
import com.lounes.gestion_reservations.model.StatutFileAttente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FileAttenteRepository extends JpaRepository<FileAttente, Long> {
    List<FileAttente> findByEmployeAndStatutNot(Employe employe, StatutFileAttente statut);
    List<FileAttente> findByStatutNot(StatutFileAttente statut);
}