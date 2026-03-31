package com.lounes.gestion_reservations.service;

import com.lounes.gestion_reservations.dto.FileAttenteRequest;
import com.lounes.gestion_reservations.dto.FileAttenteResponse;
import com.lounes.gestion_reservations.model.*;
import com.lounes.gestion_reservations.repo.*;

import org.springframework.beans.factory.annotation.Autowired;
import com.lounes.gestion_reservations.security.UserDetailsImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FileAttenteService {

    @Autowired private FileAttenteRepository fileAttenteRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private EmployeRepository employeRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private EntrepriseRepository entrepriseRepository;
    @Autowired private ConfigServiceRepository configServiceRepository;

    private static final Comparator<FileAttente> TRI_FILE = Comparator
            .comparingInt((FileAttente fa) -> statutPriority(fa.getStatut()))
            .thenComparing(fa -> fa.getReservation() != null
                    ? fa.getReservation().getHeureDebut() : LocalDateTime.MAX)
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
        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service non trouvé"));

        ConfigService config = configServiceRepository.findByServiceId(service.getId())
                .orElseThrow(() -> new RuntimeException("Configuration service introuvable"));

        FileAttente fa = new FileAttente();
        fa.setClient(client);
        fa.setService(service);
        fa.setHeureArrivee(LocalDateTime.now());
        fa.setStatut(StatutFileAttente.EN_ATTENTE);

        if (config.getTypeService() == TypeService.RESSOURCE_PARTAGEE) {
            if (request.getReservationId() != null) {
                Reservation reservation = reservationRepository.findById(request.getReservationId())
                        .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
                if (!reservation.getClient().getId().equals(client.getId()))
                    throw new RuntimeException("Cette réservation n'appartient pas à ce client !");
                // Vérifier que la réservation n'est pas déjà en file active
                if (fileAttenteRepository.existsByReservationIdAndStatutNot(reservation.getId(), StatutFileAttente.ANNULE))
                    throw new RuntimeException("Cette réservation est déjà inscrite en file d'attente.");
                fa.setReservation(reservation);
                fa.setHeureDebut(reservation.getHeureDebut());
            } else if (request.getHeureDebut() != null) {
                fa.setHeureDebut(request.getHeureDebut());
            } else {
                throw new RuntimeException("reservationId ou heureDebut obligatoire pour RESSOURCE_PARTAGEE");
            }
            fa.setEntreprise(service.getEntreprise());
        }
        else {
            if (request.getReservationId() == null)
                throw new RuntimeException("reservationId obligatoire");
            Reservation reservation = reservationRepository.findById(request.getReservationId())
                    .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
            if (!reservation.getClient().getId().equals(client.getId()))
                throw new RuntimeException("Cette réservation n'appartient pas à ce client !");
            if (fileAttenteRepository.existsByReservationIdAndStatutNot(reservation.getId(), StatutFileAttente.ANNULE))
                throw new RuntimeException("Cette réservation est déjà inscrite en file d'attente.");

            fa.setReservation(reservation);
            fa.setEntreprise(service.getEntreprise());

            if (reservation.getEmploye() != null) {
                fa.setEmploye(reservation.getEmploye());
            }
        }

        return toResponse(fileAttenteRepository.save(fa));
    }

    public void notifierPremierEnFile(Long serviceId, LocalDateTime heureDebut) {
        Optional<FileAttente> opt = fileAttenteRepository
                .findFirstEnAttenteByServiceAndCreneau(serviceId, heureDebut);
        if (opt.isPresent()) {
            FileAttente fa = opt.get();
            fa.setStatut(StatutFileAttente.APPELE);
            fileAttenteRepository.save(fa);
        }
    }

    public List<FileAttenteResponse> getAll(UserDetailsImpl userDetails) {
        boolean isGerant = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GERANT"));
        if (isGerant) {
            Entreprise entreprise = entrepriseRepository.findByGerantId(userDetails.getId())
                    .orElse(null);
            if (entreprise == null) return List.of();
            return fileAttenteRepository.findByEntrepriseIdAndStatutNot(entreprise.getId(), StatutFileAttente.ANNULE)
                    .stream().sorted(TRI_FILE).map(this::toResponse).collect(Collectors.toList());
        }
        return fileAttenteRepository.findByStatutNot(StatutFileAttente.ANNULE)
                .stream().sorted(TRI_FILE).map(this::toResponse).collect(Collectors.toList());
    }

    public List<FileAttenteResponse> getByEmployeUserId(Long userId) {
        Employe employe = employeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));
        return fileAttenteRepository.findByEmployeAndStatutNot(employe, StatutFileAttente.ANNULE)
                .stream().sorted(TRI_FILE).map(this::toResponse).collect(Collectors.toList());
    }

    public List<FileAttenteResponse> getByServiceEtCreneau(Long serviceId, LocalDateTime heureDebut) {
        return fileAttenteRepository
                .findByServiceIdAndHeureDebutAndStatutOrderByHeureArriveeAsc(
                        serviceId, heureDebut, StatutFileAttente.EN_ATTENTE)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public FileAttenteResponse appeler(Long id, Long userId) {
        FileAttente fa = getAndVerify(id, userId);
        if (fa.getStatut() != StatutFileAttente.EN_ATTENTE)
            throw new RuntimeException("Seule une entrée EN_ATTENTE peut être appelée !");
        fa.setStatut(StatutFileAttente.APPELE);
        return toResponse(fileAttenteRepository.save(fa));
    }

    public FileAttenteResponse demarrer(Long id, Long userId) {
        FileAttente fa = getAndVerify(id, userId);
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
        FileAttente fa = getAndVerify(id, userId);
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
        FileAttente fa = getAndVerify(id, userId);
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

    private FileAttente getAndVerify(Long fileId, Long userId) {
        FileAttente fa = fileAttenteRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Entrée non trouvée"));
        if (fa.getEmploye() != null) {
            Optional<Employe> employeOpt = employeRepository.findByUserId(userId);
            if (employeOpt.isPresent() && !fa.getEmploye().getId().equals(employeOpt.get().getId()))
                throw new RuntimeException("Vous ne pouvez pas modifier cette entrée !");
        }
        return fa;
    }


    private FileAttenteResponse toResponse(FileAttente fa) {
        return new FileAttenteResponse(
                fa.getId(),
                fa.getClient().getUser().getNom(),
                fa.getClient().getUser().getPrenom(),
                fa.getClient().getId(),
                fa.getEmploye() != null ? fa.getEmploye().getUser().getNom() : null,
                fa.getEmploye() != null ? fa.getEmploye().getUser().getPrenom() : null,
                fa.getEmploye() != null ? fa.getEmploye().getId() : null,
                fa.getService().getNom(),
                fa.getService().getId(),
                fa.getEntreprise() != null ? fa.getEntreprise().getNom() : null,
                fa.getEntreprise() != null ? fa.getEntreprise().getId() : null,
                fa.getReservation() != null ? fa.getReservation().getId() : null,
                fa.getReservation() != null && fa.getReservation().getRessource() != null
                        ? fa.getReservation().getRessource().getNom() : null,
                fa.getHeureDebut(),
                fa.getHeureArrivee(),
                fa.getReservation() != null ? fa.getReservation().getHeureDebut() : null,
                fa.getStatut()
        );
    }
}