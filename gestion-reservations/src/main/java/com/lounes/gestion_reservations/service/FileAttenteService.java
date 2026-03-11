package com.lounes.gestion_reservations.service;

import com.lounes.gestion_reservations.dto.FileAttenteRequest;
import com.lounes.gestion_reservations.dto.FileAttenteResponse;
import com.lounes.gestion_reservations.model.*;
import com.lounes.gestion_reservations.repo.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileAttenteService {

    @Autowired private FileAttenteRepository fileAttenteRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private EmployeRepository employeRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private ReservationRepository reservationRepository;

    private static final Comparator<FileAttente> TRI_FILE = Comparator
            .comparingInt((FileAttente fa) -> statutPriority(fa.getStatut()))
            .thenComparing(fa -> fa.getReservation() != null
                    ? fa.getReservation().getHeureDebut() : LocalDateTime.MAX) // ← corrigé
            .thenComparing(FileAttente::getHeureArrivee);

    private static int statutPriority(StatutFileAttente s) {
        return switch (s) {
            case EN_COURS   -> 0;
            case APPELE     -> 1;
            case EN_ATTENTE -> 2;
            default         -> 3;
        };
    }

    public FileAttenteResponse ajouter(FileAttenteRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        Employe employe = employeRepository.findById(request.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

        if (Boolean.TRUE.equals(employe.getArchived()))
            throw new RuntimeException("Cet employé est archivé !");
        if (employe.getEntreprise() == null)
            throw new RuntimeException("Cet employé n'est rattaché à aucune entreprise !");

        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service non trouvé"));

        if (request.getReservationId() == null)
            throw new RuntimeException("Une réservation est obligatoire pour rejoindre la file !");

        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        if (!reservation.getClient().getId().equals(client.getId()))
            throw new RuntimeException("Cette réservation n'appartient pas à ce client !");
        if (reservation.getStatut() != StatutReservation.CONFIRMEE)
            throw new RuntimeException("La réservation doit être confirmée pour rejoindre la file !");

        FileAttente fa = new FileAttente();
        fa.setClient(client);
        fa.setEmploye(employe);
        fa.setService(service);
        fa.setReservation(reservation);
        fa.setHeureArrivee(LocalDateTime.now());
        fa.setStatut(StatutFileAttente.EN_ATTENTE);
        fa.setEntreprise(employe.getEntreprise());

        return toResponse(fileAttenteRepository.save(fa));
    }

    public List<FileAttenteResponse> getAll() {
        return fileAttenteRepository.findByStatutNot(StatutFileAttente.ANNULE)
                .stream().sorted(TRI_FILE).map(this::toResponse).collect(Collectors.toList());
    }

    public List<FileAttenteResponse> getByEmployeUserId(Long userId) {
        Employe employe = employeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));
        return fileAttenteRepository.findByEmployeAndStatutNot(employe, StatutFileAttente.ANNULE)
                .stream().sorted(TRI_FILE).map(this::toResponse).collect(Collectors.toList());
    }

    public FileAttenteResponse appeler(Long id, Long userId) {
        FileAttente fa = getAndVerifyEmploye(id, userId);
        if (fa.getStatut() != StatutFileAttente.EN_ATTENTE)
            throw new RuntimeException("Seule une entrée EN_ATTENTE peut être appelée !");
        fa.setStatut(StatutFileAttente.APPELE);
        return toResponse(fileAttenteRepository.save(fa));
    }

    public FileAttenteResponse demarrer(Long id, Long userId) {
        FileAttente fa = getAndVerifyEmploye(id, userId);
        if (fa.getStatut() != StatutFileAttente.APPELE)
            throw new RuntimeException("Seule une entrée APPELE peut être démarrée !");
        fa.setStatut(StatutFileAttente.EN_COURS);
        if (fa.getReservation() != null) {
            fa.getReservation().setStatut(StatutReservation.EN_COURS);
            reservationRepository.save(fa.getReservation());
        }
        return toResponse(fileAttenteRepository.save(fa));
    }

    public FileAttenteResponse terminer(Long id, Long userId) {
        FileAttente fa = getAndVerifyEmploye(id, userId);
        if (fa.getStatut() != StatutFileAttente.EN_COURS)
            throw new RuntimeException("Seule une entrée EN_COURS peut être terminée !");
        fa.setStatut(StatutFileAttente.TERMINE);
        if (fa.getReservation() != null) {
            fa.getReservation().setStatut(StatutReservation.TERMINEE);
            reservationRepository.save(fa.getReservation());
        }
        return toResponse(fileAttenteRepository.save(fa));
    }

    public FileAttenteResponse annuler(Long id, Long userId) {
        FileAttente fa = getAndVerifyEmploye(id, userId);
        if (fa.getStatut() == StatutFileAttente.TERMINE)
            throw new RuntimeException("Impossible d'annuler une entrée déjà terminée !");
        fa.setStatut(StatutFileAttente.ANNULE);
        if (fa.getReservation() != null) {
            fa.getReservation().setStatut(StatutReservation.ANNULEE);
            reservationRepository.save(fa.getReservation());
        }
        return toResponse(fileAttenteRepository.save(fa));
    }

    public FileAttenteResponse annulerAdmin(Long id) {
        FileAttente fa = fileAttenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrée non trouvée"));
        if (fa.getStatut() == StatutFileAttente.TERMINE)
            throw new RuntimeException("Impossible d'annuler une entrée déjà terminée !");
        fa.setStatut(StatutFileAttente.ANNULE);
        if (fa.getReservation() != null) {
            fa.getReservation().setStatut(StatutReservation.ANNULEE);
            reservationRepository.save(fa.getReservation());
        }
        return toResponse(fileAttenteRepository.save(fa));
    }

    private FileAttente getAndVerifyEmploye(Long fileId, Long userId) {
        FileAttente fa = fileAttenteRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Entrée non trouvée"));
        Employe employe = employeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));
        if (!fa.getEmploye().getId().equals(employe.getId()))
            throw new RuntimeException("Vous ne pouvez pas modifier cette entrée !");
        return fa;
    }

    private FileAttenteResponse toResponse(FileAttente fa) {
        LocalDateTime dateHeureRdv = fa.getReservation() != null
                ? fa.getReservation().getHeureDebut() : null; // ← corrigé
        return new FileAttenteResponse(
                fa.getId(),
                fa.getClient().getUser().getNom(),
                fa.getClient().getUser().getPrenom(),
                fa.getEmploye().getUser().getNom(),
                fa.getEmploye().getUser().getPrenom(),
                fa.getService().getNom(),
                fa.getHeureArrivee(),
                dateHeureRdv,
                fa.getStatut()
        );
    }
}