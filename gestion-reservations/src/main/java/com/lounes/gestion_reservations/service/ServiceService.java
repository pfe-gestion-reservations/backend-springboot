package com.lounes.gestion_reservations.service;

import com.lounes.gestion_reservations.dto.ServiceRequest;
import com.lounes.gestion_reservations.dto.ServiceResponse;
import com.lounes.gestion_reservations.model.*;
import com.lounes.gestion_reservations.repo.*;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceService {

    @Autowired private ServiceRepository serviceRepository;
    @Autowired private EntrepriseRepository entrepriseRepository;
    @Autowired private EmployeRepository employeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RessourceRepository ressourceRepository;
    @Autowired private ConfigServiceRepository configServiceRepository;

    // ── Helpers ────────────────────────────────────────────

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non trouvé"));
    }

    private boolean isSuperAdmin(User user) {
        return user.getRoles().stream().anyMatch(r -> r.getName() == ERole.ROLE_SUPER_ADMIN);
    }

    private boolean isGerant(User user) {
        return user.getRoles().stream().anyMatch(r -> r.getName() == ERole.ROLE_GERANT);
    }

    private Entreprise resolveEntreprise(User user, Long entrepriseIdFromRequest) {
        if (isSuperAdmin(user)) {
            if (entrepriseIdFromRequest == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Le super admin doit spécifier une entreprise (entrepriseId)");
            return entrepriseRepository.findById(entrepriseIdFromRequest)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entreprise non trouvée"));
        }
        return entrepriseRepository.findByGerantId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Aucune entreprise trouvée pour ce gérant"));
    }

    private ServiceResponse toResponse(ServiceEntity s) {
        return new ServiceResponse(
                s.getId(), s.getNom(), s.getDescription(),
                s.getDureeMinutes(), s.getTarif(), s.getArchived(), s.getEntreprise().getId()
        );
    }

    // ── GET ALL ────────────────────────────────────────────

    public List<ServiceResponse> getAll() {
        User user = getCurrentUser();

        if (isSuperAdmin(user))
            return serviceRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());

        if (isGerant(user)) {
            Entreprise e = entrepriseRepository.findByGerantId(user.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entreprise non trouvée"));
            return serviceRepository.findByEntreprise(e).stream().map(this::toResponse).collect(Collectors.toList());
        }

        boolean isClient = user.getRoles().stream().anyMatch(r -> r.getName() == ERole.ROLE_CLIENT);
        if (isClient)
            return serviceRepository.findAll().stream()
                    .filter(s -> Boolean.FALSE.equals(s.getArchived()))
                    .map(this::toResponse).collect(Collectors.toList());

        Entreprise e = employeRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé non trouvé"))
                .getEntreprise();
        return serviceRepository.findByEntreprise(e).stream()
                .filter(s -> Boolean.FALSE.equals(s.getArchived()))
                .map(this::toResponse).collect(Collectors.toList());
    }

    public ServiceResponse getById(Long id) {
        return toResponse(serviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service non trouvé")));
    }

    // ── CREATE ─────────────────────────────────────────────

    @Transactional
    public ServiceResponse create(ServiceRequest request) {
        User user = getCurrentUser();
        Entreprise entreprise = resolveEntreprise(user, request.getEntrepriseId());
        boolean isRessourcePartagee = "RESSOURCE_PARTAGEE".equals(request.getTypeService());

        // ── Règle 1 : RESSOURCE_PARTAGEE → au moins 1 ressource obligatoire ──
        if (isRessourcePartagee) {
            if (request.getRessources() == null || request.getRessources().isEmpty())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Un service Ressource Partagée doit avoir au moins une ressource.");

            // ── Règle 2 : doublon RESSOURCE_PARTAGEE = même nom + durée + tarif + nom ressource ──
            List<ServiceEntity> mêmesServices = serviceRepository
                    .findAllByNomIgnoreCaseAndEntreprise(request.getNom(), entreprise);

            for (ServiceEntity existant : mêmesServices) {
                if (Boolean.TRUE.equals(existant.getArchived())) continue;

                boolean mêmeDurée = java.util.Objects.equals(existant.getDureeMinutes(), request.getDureeMinutes());
                boolean mêmeTarif = java.util.Objects.equals(existant.getTarif(), request.getTarif());
                if (!mêmeDurée || !mêmeTarif) continue;

                // Vérifier si une des ressources demandées existe déjà dans ce service
                List<Ressource> ressourcesExistantes = ressourceRepository.findByServiceId(existant.getId());
                for (ServiceRequest.RessourceInlineRequest rReq : request.getRessources()) {
                    boolean ressourceDoublon = ressourcesExistantes.stream()
                            .anyMatch(r -> r.getNom().equalsIgnoreCase(rReq.getNom())
                                    && !Boolean.TRUE.equals(r.getArchived()));
                    if (ressourceDoublon)
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Un service identique avec la ressource \"" + rReq.getNom() + "\" existe déjà.");
                }
            }

        } else {
            // Autres types : doublon sur nom + durée + tarif
            List<ServiceEntity> existants = serviceRepository
                    .findAllByNomIgnoreCaseAndEntreprise(request.getNom(), entreprise);
            for (ServiceEntity s : existants) {
                boolean mêmeDurée = java.util.Objects.equals(s.getDureeMinutes(), request.getDureeMinutes());
                boolean mêmeTarif = java.util.Objects.equals(s.getTarif(), request.getTarif());
                if (mêmeDurée && mêmeTarif) {
                    if (Boolean.FALSE.equals(s.getArchived()))
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Un service identique existe déjà dans cette entreprise.");
                    else
                        throw new ResponseStatusException(HttpStatus.GONE, "ARCHIVED:" + s.getId());
                }
            }
        }

        // ── Créer le service ──
        ServiceEntity service = new ServiceEntity();
        service.setNom(request.getNom());
        service.setDescription(request.getDescription());
        service.setDureeMinutes(request.getDureeMinutes());
        service.setTarif(request.getTarif());
        service.setArchived(false);
        service.setEntreprise(entreprise);
        serviceRepository.save(service);

        // ── Créer les ressources (RESSOURCE_PARTAGEE) ──
        if (isRessourcePartagee) {
            for (ServiceRequest.RessourceInlineRequest rReq : request.getRessources()) {
                Ressource ressource = new Ressource();
                ressource.setNom(rReq.getNom());
                ressource.setDescription(rReq.getDescription());
                ressource.setCapacite(rReq.getCapacite() != null ? rReq.getCapacite() : 1);
                ressource.setService(service);
                ressource.setEntreprise(entreprise);
                ressource.setArchived(false);
                ressourceRepository.save(ressource);
            }

            // ── Règle 3 : RESSOURCE_PARTAGEE → fileAttenteActive = true automatiquement ──
            ConfigService config = configServiceRepository.findByServiceId(service.getId())
                    .orElse(new ConfigService());
            config.setService(service);
            config.setTypeService(TypeService.RESSOURCE_PARTAGEE);
            config.setRessourceObligatoire(true);
            config.setEmployeObligatoire(false);
            config.setReservationEnGroupe(false);
            config.setFileAttenteActive(true); // ← toujours actif
            configServiceRepository.save(config);
        }

        return toResponse(service);
    }

    // ── UPDATE ─────────────────────────────────────────────

    public ServiceResponse update(Long id, ServiceRequest request) {
        User user = getCurrentUser();
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service non trouvé"));

        if (isGerant(user)) {
            Entreprise entreprise = entrepriseRepository.findByGerantId(user.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entreprise non trouvée"));
            if (!service.getEntreprise().getId().equals(entreprise.getId()))
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Vous ne pouvez modifier que les services de votre entreprise");
        }

        service.setNom(request.getNom());
        service.setDescription(request.getDescription());
        service.setDureeMinutes(request.getDureeMinutes());
        service.setTarif(request.getTarif());

        return toResponse(serviceRepository.save(service));
    }

    // ── ARCHIVER / DÉSARCHIVER ─────────────────────────────

    public ServiceResponse setArchived(Long id, boolean archived) {
        User user = getCurrentUser();
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service non trouvé"));

        if (isGerant(user)) {
            Entreprise entreprise = entrepriseRepository.findByGerantId(user.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entreprise non trouvée"));
            if (!service.getEntreprise().getId().equals(entreprise.getId()))
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Vous ne pouvez archiver que les services de votre entreprise");
        }

        service.setArchived(archived);
        return toResponse(serviceRepository.save(service));
    }

    // ── DELETE ─────────────────────────────────────────────

    public void delete(Long id) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service non trouvé"));
        serviceRepository.delete(service);
    }
}