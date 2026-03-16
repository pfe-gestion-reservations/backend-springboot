package com.lounes.gestion_reservations.repo;

import com.lounes.gestion_reservations.model.Client;
import com.lounes.gestion_reservations.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByCreatedBy(String createdBy);
    List<Client> findByArchivedFalse();
    List<Client> findByArchivedTrue();
    Optional<Client> findByUserId(Long userId);
    Optional<Client> findByUserIdAndArchivedTrue(Long userId);
    Optional<Client> findByNumtel(String numtel);

    // Charge toutes les entreprises de chaque client en une seule requête
    @Query("SELECT DISTINCT c FROM Client c LEFT JOIN FETCH c.entreprises e LEFT JOIN FETCH e.secteur")
    List<Client> findAllWithEntreprises();

    // Clients d'une entreprise spécifique
    @Query("SELECT DISTINCT c FROM Client c JOIN FETCH c.entreprises e LEFT JOIN FETCH e.secteur WHERE e.id = :entrepriseId")
    List<Client> findByEntrepriseId(Long entrepriseId);

    // Vérifie si un client est déjà dans une entreprise donnée
    @Query("SELECT COUNT(c) > 0 FROM Client c JOIN c.entreprises e WHERE c.id = :clientId AND e.id = :entrepriseId")
    boolean isClientInEntreprise(Long clientId, Long entrepriseId);

    @Query("SELECT c FROM Client c LEFT JOIN FETCH c.entreprises e LEFT JOIN FETCH e.secteur WHERE c.id = :id")
    Optional<Client> findByIdWithEntreprises(Long id);

    @Query("SELECT c FROM Client c LEFT JOIN FETCH c.entreprises e LEFT JOIN FETCH e.secteur WHERE c.user.email = :email")
    Optional<Client> findByUserEmailWithEntreprises(String email);

    Optional<Client> findByUser(User user);
}