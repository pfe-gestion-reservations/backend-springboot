package com.lounes.gestion_reservations.repo;

import com.lounes.gestion_reservations.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    // Clients créés par un employé (createdBy = email)
    List<Client> findByCreatedBy(String createdBy);

    // Archivage
    List<Client> findByArchivedFalse();
    List<Client> findByArchivedTrue();

    // Trouver par userId (utilisé dans AvisService, FileAttenteService)
    Optional<Client> findByUserId(Long userId);

    // Pour détecter un email déjà archivé
    Optional<Client> findByUserIdAndArchivedTrue(Long userId);
}