package com.lounes.gestion_reservations.service;

import com.lounes.gestion_reservations.dto.CreateReservationRequest;
import com.lounes.gestion_reservations.dto.ReservationResponse;
import com.lounes.gestion_reservations.model.*;
import com.lounes.gestion_reservations.repo.*;
import com.lounes.gestion_reservations.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    @Autowired private ReservationRepository    reservationRepo;
    @Autowired private ClientRepository         clientRepo;
    @Autowired private EmployeRepository        employeRepo;
    @Autowired private ServiceRepository        serviceRepo;
    @Autowired private EntrepriseRepository     entrepriseRepo;
    @Autowired private DisponibiliteRepository  disponibiliteRepo;
    @Autowired private RessourceRepository      ressourceRepo;
    @Autowired private FileAttenteRepository    fileAttenteRepo;

    // ── helpers ──────────────────────────────────────────────────────────────
    private UserDetailsImpl getCurrentUserDetails() {
        return (UserDetailsImpl) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
    }

    private boolean isSuperAdmin(UserDetailsImpl ud) {
        return ud.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    // Retourne null pour SUPER_ADMIN (l'entreprise sera déduite du service)
    private Entreprise getCurrentEntreprise() {
        UserDetailsImpl ud = getCurrentUserDetails();
        if (isSuperAdmin(ud)) return null;
        return entrepriseRepo.findByGerantId(ud.getId())
                .orElseGet(() -> entrepriseRepo.findByEmployeUserId(ud.getId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Entreprise introuvable")));
    }

    // ── CALCUL DU PRIX ───────────────────────────────────────────────────────
    private Double calculerPrix(ServiceEntity service, ConfigService config, int nombrePersonnes) {
        if (service.getTarif() == null) return null;
        boolean parPersonne = config != null && Boolean.TRUE.equals(config.getTarifParPersonne());
        return parPersonne ? service.getTarif() * nombrePersonnes : service.getTarif();
    }

    // ── VALIDATION DISPONIBILITÉ ─────────────────────────────────────────────
    private void validerDisponibilite(Long serviceId, LocalDateTime heureDebut) {
        JourSemaine jour = JourSemaine.valueOf(
                heureDebut.getDayOfWeek().name()
                        .replace("MONDAY","LUNDI").replace("TUESDAY","MARDI")
                        .replace("WEDNESDAY","MERCREDI").replace("THURSDAY","JEUDI")
                        .replace("FRIDAY","VENDREDI").replace("SATURDAY","SAMEDI")
                        .replace("SUNDAY","DIMANCHE")
        );
        LocalTime heure = heureDebut.toLocalTime();
        List<Disponibilite> dispos = disponibiliteRepo
                .findByServiceIdAndJourAndHeureDebutLessThanEqualAndHeureFinGreaterThan(
                        serviceId, jour, heure, heure);
        if (dispos.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ce service n'est pas disponible le " + jour.name() + " à " + heure);
    }

    // ── TROUVER RESSOURCE OPTIMALE ───────────────────────────────────────────
    private Ressource trouverRessourceOptimale(Long serviceId, LocalDateTime heureDebut,
                                               LocalDateTime heureFin, Long excludeReservationId) {
        List<Ressource> ressources = ressourceRepo.findByServiceIdAndArchivedFalse(serviceId);
        if (ressources.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Aucune ressource disponible pour ce service.");
        for (Ressource ressource : ressources) {
            boolean occupee = reservationRepo.isRessourceOccupee(
                    ressource.getId(), heureDebut, heureFin, excludeReservationId);
            if (!occupee) return ressource;
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Toutes les ressources sont occupées sur ce créneau.");
    }

    private Ressource trouverRessourceLibre(Long serviceId, LocalDateTime heureDebut,
                                            LocalDateTime heureFin, Long excludeReservationId) {
        return trouverRessourceOptimale(serviceId, heureDebut, heureFin, excludeReservationId);
    }

    // ── NOTIFIER PREMIER EN FILE ─────────────────────────────────────────────
    private void notifierPremierEnFile(Long serviceId, LocalDateTime heureDebut) {
        Optional<FileAttente> opt = fileAttenteRepo
                .findFirstEnAttenteByServiceAndCreneau(serviceId, heureDebut);
        opt.ifPresent(fa -> {
            fa.setStatut(StatutFileAttente.APPELE);
            fileAttenteRepo.save(fa);
        });
    }

    // ── toResponse ───────────────────────────────────────────────────────────
    private ReservationResponse toResponse(Reservation r) {
        ReservationResponse res = new ReservationResponse();
        res.setId(r.getId());
        res.setClientId(r.getClient().getId());
        res.setClientNom(r.getClient().getUser().getNom());
        res.setClientPrenom(r.getClient().getUser().getPrenom());
        res.setEmployeId(r.getEmploye() != null ? r.getEmploye().getId() : null);
        res.setEmployeNom(r.getEmploye() != null ? r.getEmploye().getUser().getNom() : null);
        res.setEmployePrenom(r.getEmploye() != null ? r.getEmploye().getUser().getPrenom() : null);
        res.setRessourceId(r.getRessource() != null ? r.getRessource().getId() : null);
        res.setRessourceNom(r.getRessource() != null ? r.getRessource().getNom() : null);
        res.setServiceId(r.getService().getId());
        res.setServiceNom(r.getService().getNom());
        res.setHeureDebut(r.getHeureDebut());
        res.setHeureFin(r.getHeureFin());
        res.setNombrePersonnes(r.getNombrePersonnes());
        res.setPrixTotal(r.getPrixTotal());
        res.setStatut(r.getStatut().name());
        res.setNotes(r.getNotes());
        return res;
    }

    // ── GET ALL ──────────────────────────────────────────────────────────────
    public List<ReservationResponse> getAll() {
        UserDetailsImpl ud = getCurrentUserDetails();
        boolean isSuperAdmin = isSuperAdmin(ud);
        boolean isGerant = ud.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GERANT"));
        boolean isEmploye = ud.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYE"));

        if (isSuperAdmin)
            return reservationRepo.findAll().stream()
                    .map(this::toResponse).collect(Collectors.toList());
        if (isGerant) {
            Entreprise ent = entrepriseRepo.findByGerantId(ud.getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Entreprise du gérant introuvable"));
            return reservationRepo.findByEntrepriseId(ent.getId())
                    .stream().map(this::toResponse).collect(Collectors.toList());
        }
        if (isEmploye) {
            Entreprise ent = entrepriseRepo.findByEmployeUserId(ud.getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Entreprise de l'employé introuvable"));
            return reservationRepo.findByEntrepriseId(ent.getId())
                    .stream().map(this::toResponse).collect(Collectors.toList());
        }
        Client client = clientRepo.findByUserId(ud.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Client introuvable"));
        return reservationRepo.findByClientId(client.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── CREATE ───────────────────────────────────────────────────────────────
    public ReservationResponse create(CreateReservationRequest req) {
        validerDisponibilite(req.getServiceId(), req.getHeureDebut());

        ServiceEntity service = serviceRepo.findById(req.getServiceId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Service introuvable"));

        ConfigService config = service.getConfig();
        int duree = config != null && config.getDureeMinutes() != null
                ? config.getDureeMinutes() : service.getDureeMinutes();
        LocalDateTime heureFin = req.getHeureDebut().plusMinutes(duree);

        Client client = clientRepo.findById(req.getClientId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Client introuvable"));

        // Vérifier chevauchement pour ce client sur ce service
        boolean overlap = reservationRepo.hasClientOverlap(
                client.getId(), service.getId(), req.getHeureDebut(), heureFin, null);
        if (overlap)
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ce client a déjà une réservation qui chevauche ce créneau pour ce service.");

        // SUPER_ADMIN → entreprise déduite du service choisi
        Entreprise ent = getCurrentEntreprise();
        if (ent == null) ent = service.getEntreprise();

        Reservation r = new Reservation();
        r.setClient(client);
        r.setService(service);
        r.setEntreprise(ent);
        r.setHeureDebut(req.getHeureDebut());
        r.setHeureFin(heureFin);
        r.setNombrePersonnes(req.getNombrePersonnes() != null ? req.getNombrePersonnes() : 1);
        r.setNotes(req.getNotes());
        r.setStatut(StatutReservation.EN_ATTENTE);

        String typeService = config != null ? config.getTypeService().name() : "";

        switch (typeService) {
            case "RESSOURCE_PARTAGEE" -> {
                // La ressource gère elle-même le chevauchement via trouverRessourceOptimale
                Ressource ressource = trouverRessourceOptimale(
                        service.getId(), req.getHeureDebut(), heureFin, null);
                r.setRessource(ressource);
                r.setPrixTotal(calculerPrix(service, config, r.getNombrePersonnes()));
            }
            case "EMPLOYE_DEDIE" -> {
                if (req.getEmployeId() != null) {
                    Employe employe = employeRepo.findById(req.getEmployeId())
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND, "Employé introuvable"));
                    boolean conflitEmploye = reservationRepo.findByEmployeId(employe.getId())
                            .stream()
                            .filter(rv -> rv.getStatut() != StatutReservation.ANNULEE)
                            .anyMatch(rv -> rv.getHeureDebut().isBefore(heureFin)
                                    && rv.getHeureFin().isAfter(req.getHeureDebut()));
                    if (conflitEmploye)
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Cet employé est déjà réservé sur ce créneau.");
                    r.setEmploye(employe);
                } else {
                    // Pas d'employé spécifié → vérifier chevauchement global du service
                    if (reservationRepo.hasServiceOverlap(service.getId(), req.getHeureDebut(), heureFin, null))
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Ce créneau est déjà occupé pour ce service.");
                }
                r.setPrixTotal(calculerPrix(service, config, r.getNombrePersonnes()));
            }
            case "HYBRIDE" -> {
                if (req.getEmployeId() != null) {
                    Employe employe = employeRepo.findById(req.getEmployeId())
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND, "Employé introuvable"));
                    boolean conflitEmploye = reservationRepo.findByEmployeId(employe.getId())
                            .stream()
                            .filter(rv -> rv.getStatut() != StatutReservation.ANNULEE)
                            .anyMatch(rv -> rv.getHeureDebut().isBefore(heureFin)
                                    && rv.getHeureFin().isAfter(req.getHeureDebut()));
                    if (conflitEmploye)
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Cet employé est déjà réservé sur ce créneau.");
                    r.setEmploye(employe);
                }
                Ressource ressource = trouverRessourceLibre(
                        service.getId(), req.getHeureDebut(), heureFin, null);
                r.setRessource(ressource);
                r.setPrixTotal(calculerPrix(service, config, r.getNombrePersonnes()));
            }
            case "FILE_ATTENTE_PURE" -> {
                // File d'attente : vérifier chevauchement global du service
                if (reservationRepo.hasServiceOverlap(service.getId(), req.getHeureDebut(), heureFin, null))
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Ce créneau est déjà occupé pour ce service.");
                r.setPrixTotal(calculerPrix(service, config, r.getNombrePersonnes()));
            }
            default -> {
                if (reservationRepo.hasServiceOverlap(service.getId(), req.getHeureDebut(), heureFin, null))
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Ce créneau est déjà réservé pour ce service.");
                if (req.getEmployeId() != null) {
                    Employe employe = employeRepo.findById(req.getEmployeId())
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND, "Employé introuvable"));
                    r.setEmploye(employe);
                }
                r.setPrixTotal(calculerPrix(service, config, r.getNombrePersonnes()));
            }
        }

        return toResponse(reservationRepo.save(r));
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    public ReservationResponse update(Long id, CreateReservationRequest req) {
        Reservation r = reservationRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Réservation introuvable"));

        if (!r.getHeureDebut().equals(req.getHeureDebut()) ||
                !r.getService().getId().equals(req.getServiceId())) {
            validerDisponibilite(req.getServiceId(), req.getHeureDebut());
        }

        ServiceEntity service = serviceRepo.findById(req.getServiceId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Service introuvable"));

        ConfigService config = service.getConfig();
        int duree = config != null && config.getDureeMinutes() != null
                ? config.getDureeMinutes() : service.getDureeMinutes();
        LocalDateTime heureFin = req.getHeureDebut().plusMinutes(duree);
        String typeService = config != null ? config.getTypeService().name() : "";

        // Vérifier chevauchement pour ce client (en excluant la réservation courante)
        boolean overlap = reservationRepo.hasClientOverlap(
                r.getClient().getId(), service.getId(), req.getHeureDebut(), heureFin, id);
        if (overlap)
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ce client a déjà une réservation qui chevauche ce créneau pour ce service.");

        // Vérifier chevauchement global du service pour les types sans ressource/employé
        if ("FILE_ATTENTE_PURE".equals(typeService) ||
                ("EMPLOYE_DEDIE".equals(typeService) && req.getEmployeId() == null)) {
            if (reservationRepo.hasServiceOverlap(service.getId(), req.getHeureDebut(), heureFin, id))
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Ce créneau est déjà occupé pour ce service.");
        }

        r.setService(service);
        r.setHeureDebut(req.getHeureDebut());
        r.setHeureFin(heureFin);
        r.setNotes(req.getNotes());

        if ("RESSOURCE_PARTAGEE".equals(typeService) || "HYBRIDE".equals(typeService)) {
            Ressource ressource = trouverRessourceLibre(
                    service.getId(), req.getHeureDebut(), heureFin, id);
            r.setRessource(ressource);
        }
        if (req.getEmployeId() != null &&
                ("EMPLOYE_DEDIE".equals(typeService) || "HYBRIDE".equals(typeService))) {
            Employe employe = employeRepo.findById(req.getEmployeId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Employé introuvable"));
            r.setEmploye(employe);
        }
        r.setPrixTotal(calculerPrix(service, config, r.getNombrePersonnes()));
        return toResponse(reservationRepo.save(r));
    }

    // ── UPDATE STATUT ────────────────────────────────────────────────────────
    public ReservationResponse updateStatut(Long id, String statut) {
        Reservation r = reservationRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Réservation introuvable"));

        StatutReservation nouveauStatut = StatutReservation.valueOf(statut);
        r.setStatut(nouveauStatut);
        reservationRepo.save(r);

        if (nouveauStatut == StatutReservation.ANNULEE && r.getRessource() != null) {
            ConfigService config = r.getService().getConfig();
            if (config != null && config.getTypeService() == TypeService.RESSOURCE_PARTAGEE) {
                notifierPremierEnFile(r.getService().getId(), r.getHeureDebut());
            }
        }

        return toResponse(r);
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public void delete(Long id) {
        Reservation r = reservationRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Réservation introuvable"));
        if (r.getRessource() != null) {
            ConfigService config = r.getService().getConfig();
            if (config != null && config.getTypeService() == TypeService.RESSOURCE_PARTAGEE) {
                notifierPremierEnFile(r.getService().getId(), r.getHeureDebut());
            }
        }
        // Supprimer d'abord les entrées de file d'attente liées
        fileAttenteRepo.deleteByReservationId(id);
        reservationRepo.deleteById(id);
    }
}