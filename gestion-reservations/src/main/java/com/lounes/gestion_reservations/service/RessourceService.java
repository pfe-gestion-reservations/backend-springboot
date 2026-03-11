package com.lounes.gestion_reservations.service;

import com.lounes.gestion_reservations.dto.RessourceRequest;
import com.lounes.gestion_reservations.dto.RessourceResponse;
import com.lounes.gestion_reservations.model.Entreprise;
import com.lounes.gestion_reservations.model.Ressource;
import com.lounes.gestion_reservations.model.ServiceEntity;
import com.lounes.gestion_reservations.repo.EntrepriseRepository;
import com.lounes.gestion_reservations.repo.RessourceRepository;
import com.lounes.gestion_reservations.repo.ServiceRepository;
import com.lounes.gestion_reservations.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RessourceService {

    @Autowired private RessourceRepository ressourceRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private EntrepriseRepository entrepriseRepository;

    private UserDetailsImpl getCurrentUserDetails() {
        return (UserDetailsImpl) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
    }

    private boolean isSuperAdmin(UserDetailsImpl ud) {
        return ud.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    // Pour les opérations GÉRANT — résout son entreprise
    private Entreprise getEntrepriseForGerant(Long userId) {
        return entrepriseRepository.findByGerantId(userId)
                .orElseThrow(() -> new RuntimeException("Entreprise introuvable pour ce gérant"));
    }

    // ── CREATE ───────────────────────────────────────────────
    // SUPER_ADMIN → entreprise déduite du service cible
    // GÉRANT      → vérifie que le service appartient à son entreprise
    public RessourceResponse create(RessourceRequest request) {
        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service non trouvé"));

        UserDetailsImpl ud = getCurrentUserDetails();
        Entreprise entreprise;
        if (isSuperAdmin(ud)) {
            entreprise = service.getEntreprise();
        } else {
            entreprise = getEntrepriseForGerant(ud.getId());
            if (!service.getEntreprise().getId().equals(entreprise.getId()))
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Ce service n'appartient pas à votre entreprise");
        }

        Ressource r = new Ressource();
        r.setNom(request.getNom());
        r.setDescription(request.getDescription());
        r.setCapacite(request.getCapacite() != null ? request.getCapacite() : 1);
        r.setService(service);
        r.setEntreprise(entreprise);
        r.setArchived(false);

        // ── Vérifier doublon : même nom de ressource dans le même service ──
        if (ressourceRepository.existsByNomIgnoreCaseAndServiceId(request.getNom(), service.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Une ressource avec ce nom existe déjà pour ce service");
        }

        return toResponse(ressourceRepository.save(r));
    }

    // ── GET BY SERVICE ───────────────────────────────────────
    public List<RessourceResponse> getByService(Long serviceId) {
        return ressourceRepository.findByServiceIdAndArchivedFalse(serviceId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── GET ALL ─────────────────────────────────────────────
    // SUPER_ADMIN → toutes les ressources | GÉRANT → son entreprise
    public List<RessourceResponse> getAll() {
        UserDetailsImpl ud = getCurrentUserDetails();
        if (isSuperAdmin(ud)) {
            return ressourceRepository.findAll()
                    .stream().map(this::toResponse).collect(Collectors.toList());
        }
        Entreprise ent = getEntrepriseForGerant(ud.getId());
        return ressourceRepository.findByEntrepriseId(ent.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── UPDATE ───────────────────────────────────────────────
    public RessourceResponse update(Long id, RessourceRequest request) {
        Ressource r = ressourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ressource non trouvée"));

        r.setNom(request.getNom());
        r.setDescription(request.getDescription());
        if (request.getCapacite() != null) r.setCapacite(request.getCapacite());

        if (request.getServiceId() != null &&
                !request.getServiceId().equals(r.getService().getId())) {
            ServiceEntity service = serviceRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service non trouvé"));
            r.setService(service);
        }

        return toResponse(ressourceRepository.save(r));
    }

    // ── ARCHIVER / DÉSARCHIVER ───────────────────────────────
    public RessourceResponse archiver(Long id) {
        Ressource r = ressourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ressource non trouvée"));
        r.setArchived(true);
        return toResponse(ressourceRepository.save(r));
    }

    public RessourceResponse desarchiver(Long id) {
        Ressource r = ressourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ressource non trouvée"));
        r.setArchived(false);
        return toResponse(ressourceRepository.save(r));
    }

    // ── toResponse ───────────────────────────────────────────
    private RessourceResponse toResponse(Ressource r) {
        return new RessourceResponse(
                r.getId(),
                r.getNom(),
                r.getDescription(),
                r.getCapacite(),
                r.getArchived(),
                r.getService().getId(),
                r.getService().getNom(),
                r.getEntreprise().getId()
        );
    }
}