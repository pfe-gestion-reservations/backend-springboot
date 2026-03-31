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
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private FileAttenteRepository fileAttenteRepository;
    @Autowired private DisponibiliteRepository disponibiliteRepository;


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
                s.getDureeMinutes(), s.getTarif(), s.getEntreprise().getId()
        );
    }


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
                    .map(this::toResponse).collect(Collectors.toList());

        Entreprise e = employeRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé non trouvé"))
                .getEntreprise();
        return serviceRepository.findByEntreprise(e).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public ServiceResponse getById(Long id) {
        return toResponse(serviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service non trouvé")));
    }

    public List<ServiceResponse> getByEntreprise(Long entrepriseId) {
        Entreprise e = entrepriseRepository.findById(entrepriseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entreprise non trouvée"));
        return serviceRepository.findByEntreprise(e).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }


    @Transactional
    public ServiceResponse create(ServiceRequest request) {
        User user = getCurrentUser();
        Entreprise entreprise = resolveEntreprise(user, request.getEntrepriseId());
        boolean isRessourcePartagee = "RESSOURCE_PARTAGEE".equals(request.getTypeService());

        if (isRessourcePartagee) {
            if (request.getRessources() == null || request.getRessources().isEmpty())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Un service Ressource Partagée doit avoir au moins une ressource.");

            List<ServiceEntity> mêmesServices = serviceRepository
                    .findAllByNomIgnoreCaseAndEntreprise(request.getNom(), entreprise);

            for (ServiceEntity existant : mêmesServices) {
                boolean mêmeDurée = java.util.Objects.equals(existant.getDureeMinutes(), request.getDureeMinutes());
                boolean mêmeTarif = java.util.Objects.equals(existant.getTarif(), request.getTarif());
                if (!mêmeDurée || !mêmeTarif) continue;

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
            List<ServiceEntity> existants = serviceRepository
                    .findAllByNomIgnoreCaseAndEntreprise(request.getNom(), entreprise);
            for (ServiceEntity s : existants) {
                boolean mêmeDurée = java.util.Objects.equals(s.getDureeMinutes(), request.getDureeMinutes());
                boolean mêmeTarif = java.util.Objects.equals(s.getTarif(), request.getTarif());
                if (mêmeDurée && mêmeTarif) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Un service identique existe déjà dans cette entreprise.");
                }
            }
        }

        ServiceEntity service = new ServiceEntity();
        service.setNom(request.getNom());
        service.setDescription(request.getDescription());
        service.setDureeMinutes(request.getDureeMinutes());
        service.setTarif(request.getTarif());
        service.setEntreprise(entreprise);
        serviceRepository.save(service);

        if (isRessourcePartagee) {
            for (ServiceRequest.RessourceInlineRequest rReq : request.getRessources()) {
                Ressource ressource = new Ressource();
                ressource.setNom(rReq.getNom());
                ressource.setDescription(rReq.getDescription());
                ressource.setService(service);
                ressource.setEntreprise(entreprise);
                ressource.setArchived(false);
                ressourceRepository.save(ressource);
            }

            ConfigService config = configServiceRepository.findByServiceId(service.getId())
                    .orElse(new ConfigService());
            config.setService(service);
            config.setTypeService(TypeService.RESSOURCE_PARTAGEE);
            config.setDureeMinutes(request.getDureeMinutes());
            config.setCapaciteMinPersonnes(request.getCapaciteMinPersonnes());
            config.setCapaciteMaxPersonnes(request.getCapaciteMaxPersonnes());
            config.setAnnulationHeures(request.getAnnulationHeures());
            config.setAvanceReservationJours(request.getAvanceReservationJours());
            config.setRessourceObligatoire(true);
            config.setEmployeObligatoire(false);
            config.setReservationEnGroupe(
                    request.getCapaciteMaxPersonnes() != null && request.getCapaciteMaxPersonnes() > 1
            );
            config.setFileAttenteActive(true);
            configServiceRepository.save(config);
        }

        return toResponse(service);
    }


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


    @Transactional
    public void delete(Long id) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service non trouvé"));

        boolean isRessourcePartagee = configServiceRepository.findByServiceId(id)
                .map(c -> c.getTypeService() == TypeService.RESSOURCE_PARTAGEE)
                .orElse(false);

        boolean hasReservations = !reservationRepository.findByServiceId(id).isEmpty();
        boolean hasFileAttente  = !fileAttenteRepository.findByStatutNot(StatutFileAttente.ANNULE)
                .stream().filter(fa -> fa.getService().getId().equals(id)).toList().isEmpty();

        if (hasReservations || hasFileAttente) {
            List<String> details = new java.util.ArrayList<>();
            if (hasReservations) {
                long nb = reservationRepository.findByServiceId(id).size();
                details.add(nb + " réservation" + (nb > 1 ? "s" : ""));
            }
            if (hasFileAttente) {
                long nb = fileAttenteRepository.findByStatutNot(StatutFileAttente.ANNULE)
                        .stream().filter(fa -> fa.getService().getId().equals(id)).count();
                details.add(nb + " entrée" + (nb > 1 ? "s" : "") + " en file d'attente");
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ce service ne peut pas être supprimé car il est lié à : "
                            + String.join(", ", details) + ".");
        }

        disponibiliteRepository.findByServiceId(id)
                .forEach(d -> disponibiliteRepository.deleteById(d.getId()));

        ressourceRepository.findByServiceId(id)
                .forEach(r -> ressourceRepository.deleteById(r.getId()));

        configServiceRepository.findByServiceId(id)
                .ifPresent(c -> configServiceRepository.deleteById(c.getId()));
        serviceRepository.delete(service);
    }
}