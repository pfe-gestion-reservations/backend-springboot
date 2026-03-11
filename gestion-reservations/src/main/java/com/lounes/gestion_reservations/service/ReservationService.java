package com.lounes.gestion_reservations.service;

import com.lounes.gestion_reservations.dto.CreateReservationRequest;
import com.lounes.gestion_reservations.dto.ReservationResponse;
import com.lounes.gestion_reservations.model.*;
import com.lounes.gestion_reservations.repo.*;
import com.lounes.gestion_reservations.security.UserDetailsImpl;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    @Autowired private ReservationRepository reservationRepo;
    @Autowired private ClientRepository clientRepo;
    @Autowired private EmployeRepository employeRepo;
    @Autowired private ServiceRepository serviceRepo;
    @Autowired private EntrepriseRepository entrepriseRepo;
    @Autowired private DisponibiliteRepository disponibiliteRepo;

    // ── helper ──────────────────────────────────────────────
    private Entreprise getCurrentEntreprise() {
        UserDetailsImpl ud = (UserDetailsImpl) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return entrepriseRepo.findByGerantId(ud.getId())
                .orElseGet(() -> entrepriseRepo.findByEmployeUserId(ud.getId())
                        .orElseThrow(() -> new RuntimeException("Entreprise introuvable")));
    }

    private ReservationResponse toResponse(Reservation r) {
        ReservationResponse res = new ReservationResponse();
        res.setId(r.getId());
        res.setClientId(r.getClient().getId());
        res.setClientNom(r.getClient().getUser().getNom());
        res.setClientPrenom(r.getClient().getUser().getPrenom());
        res.setEmployeId(r.getEmploye() != null ? r.getEmploye().getId() : null);
        res.setEmployeNom(r.getEmploye() != null ? r.getEmploye().getUser().getNom() : null);
        res.setEmployePrenom(r.getEmploye() != null ? r.getEmploye().getUser().getPrenom() : null);
        res.setServiceId(r.getService().getId());
        res.setServiceNom(r.getService().getNom());
        res.setHeureDebut(r.getHeureDebut());   // ← remplace dateHeure
        res.setHeureFin(r.getHeureFin());       // ← nouveau
        res.setNombrePersonnes(r.getNombrePersonnes()); // ← nouveau
        res.setStatut(r.getStatut().name());
        res.setNotes(r.getNotes());
        return res;
    }

    // ── VALIDATION DISPONIBILITÉ ─────────────────────────────
    private void validerDisponibilite(Long serviceId, java.time.LocalDateTime heureDebut) {
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
                        serviceId, jour, heure, heure
                );

        if (dispos.isEmpty()) {
            throw new RuntimeException(
                    "Ce service n'est pas disponible le " + jour.name() +
                            " à " + heure + ". Veuillez choisir un créneau disponible."
            );
        }
    }

    // ── CRUD ────────────────────────────────────────────────
    // ✅ Filtrage selon le rôle :
    //    SUPER_ADMIN -> toutes | GERANT/EMPLOYE -> leur entreprise | CLIENT -> ses réservations
    public List<ReservationResponse> getAll() {
        UserDetailsImpl ud = (UserDetailsImpl) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        boolean isSuperAdmin = ud.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        boolean isGerant = ud.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GERANT"));
        boolean isEmploye = ud.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYE"));

        if (isSuperAdmin) {
            return reservationRepo.findAll().stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }

        if (isGerant) {
            Entreprise ent = entrepriseRepo.findByGerantId(ud.getId())
                    .orElseThrow(() -> new RuntimeException("Entreprise du gerant introuvable"));
            return reservationRepo.findByEntrepriseId(ent.getId())
                    .stream().map(this::toResponse).collect(Collectors.toList());
        }

        if (isEmploye) {
            Entreprise ent = entrepriseRepo.findByEmployeUserId(ud.getId())
                    .orElseThrow(() -> new RuntimeException("Entreprise de l'employe introuvable"));
            return reservationRepo.findByEntrepriseId(ent.getId())
                    .stream().map(this::toResponse).collect(Collectors.toList());
        }

        // CLIENT - uniquement ses propres reservations
        Client client = clientRepo.findByUserId(ud.getId())
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
        return reservationRepo.findByClientId(client.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ReservationResponse create(CreateReservationRequest req) {
        validerDisponibilite(req.getServiceId(), req.getHeureDebut());

        boolean conflit = reservationRepo.existsByServiceIdAndHeureDebutAndStatutNot(
                req.getServiceId(), req.getHeureDebut(), StatutReservation.ANNULEE
        );
        if (conflit) throw new RuntimeException("Ce créneau est déjà réservé pour ce service.");

        Client client = clientRepo.findById(req.getClientId())
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
        ServiceEntity service = serviceRepo.findById(req.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service introuvable"));
        Entreprise ent = getCurrentEntreprise();

        Employe employe = null;
        if (req.getEmployeId() != null) {
            employe = employeRepo.findById(req.getEmployeId())
                    .orElseThrow(() -> new RuntimeException("Employé introuvable"));
        }

        // Calcul heureFin depuis ConfigService
        int duree = service.getConfig() != null && service.getConfig().getDureeMinutes() != null
                ? service.getConfig().getDureeMinutes()
                : service.getDureeMinutes();

        Reservation r = new Reservation();
        r.setClient(client);
        r.setEmploye(employe);
        r.setService(service);
        r.setEntreprise(ent);
        r.setHeureDebut(req.getHeureDebut());
        r.setHeureFin(req.getHeureDebut().plusMinutes(duree));
        r.setNombrePersonnes(req.getNombrePersonnes() != null ? req.getNombrePersonnes() : 1);
        r.setNotes(req.getNotes());
        r.setStatut(StatutReservation.EN_ATTENTE);

        reservationRepo.save(r);
        return toResponse(r);
    }

    public ReservationResponse update(Long id, CreateReservationRequest req) {
        Reservation r = reservationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable"));

        if (!r.getHeureDebut().equals(req.getHeureDebut()) ||
                !r.getService().getId().equals(req.getServiceId())) {
            validerDisponibilite(req.getServiceId(), req.getHeureDebut());
        }

        ServiceEntity service = serviceRepo.findById(req.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service introuvable"));

        int duree = service.getConfig() != null && service.getConfig().getDureeMinutes() != null
                ? service.getConfig().getDureeMinutes()
                : service.getDureeMinutes();

        r.setService(service);
        r.setHeureDebut(req.getHeureDebut());
        r.setHeureFin(req.getHeureDebut().plusMinutes(duree));
        r.setNotes(req.getNotes());

        if (req.getEmployeId() != null) {
            Employe employe = employeRepo.findById(req.getEmployeId())
                    .orElseThrow(() -> new RuntimeException("Employé introuvable"));
            r.setEmploye(employe);
        }

        reservationRepo.save(r);
        return toResponse(r);
    }

    public ReservationResponse updateStatut(Long id, String statut) {
        Reservation r = reservationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable"));
        r.setStatut(StatutReservation.valueOf(statut));
        reservationRepo.save(r);
        return toResponse(r);
    }

    public void delete(Long id) {
        reservationRepo.deleteById(id);
    }
}