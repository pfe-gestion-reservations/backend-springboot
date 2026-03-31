package com.lounes.gestion_reservations.service;

import com.lounes.gestion_reservations.dto.EntrepriseRequest;
import com.lounes.gestion_reservations.dto.EntrepriseResponse;
import com.lounes.gestion_reservations.model.*;
import com.lounes.gestion_reservations.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EntrepriseService {

    @Autowired private EntrepriseRepository entrepriseRepository;
    @Autowired private SecteurRepository secteurRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;

    public EntrepriseResponse create(EntrepriseRequest request) {
        if (entrepriseRepository.existsByTelephone(request.getTelephone()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Une entreprise avec ce numéro de téléphone existe déjà !");

        Secteur secteur = secteurRepository.findById(request.getSecteurId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Secteur non trouvé"));

        User gerant = userRepository.findById(request.getGerantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gérant non trouvé"));
        if (entrepriseRepository.findByGerantId(gerant.getId()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ce gérant est déjà assigné à une autre entreprise !");

        Role roleGerant = roleRepository.findByName(ERole.ROLE_GERANT)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Role GERANT non trouvé"));
        gerant.getRoles().add(roleGerant);
        userRepository.save(gerant);

        Entreprise entreprise = new Entreprise();
        entreprise.setNom(request.getNom());
        entreprise.setAdresse(request.getAdresse());
        entreprise.setTelephone(request.getTelephone());
        entreprise.setSecteur(secteur);
        entreprise.setGerant(gerant);

        return toResponse(entrepriseRepository.save(entreprise));
    }

    public List<EntrepriseResponse> getAll() {
        return entrepriseRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<EntrepriseResponse> getBySecteur(Long secteurId) {
        return entrepriseRepository.findBySecteurId(secteurId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public EntrepriseResponse getById(Long id) {
        return toResponse(entrepriseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entreprise non trouvée")));
    }

    public EntrepriseResponse update(Long id, EntrepriseRequest request) {
        Entreprise entreprise = entrepriseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entreprise non trouvée"));
        if (entrepriseRepository.existsByTelephoneAndIdNot(request.getTelephone(), id))
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Une entreprise avec ce numéro de téléphone existe déjà !");

        Secteur secteur = secteurRepository.findById(request.getSecteurId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Secteur non trouvé"));

        entreprise.setNom(request.getNom());
        entreprise.setAdresse(request.getAdresse());
        entreprise.setTelephone(request.getTelephone());
        entreprise.setSecteur(secteur);

        if (!entreprise.getGerant().getId().equals(request.getGerantId())) {
            User newGerant = userRepository.findById(request.getGerantId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gérant non trouvé"));
            if (entrepriseRepository.findByGerantId(newGerant.getId()).isPresent())
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Ce gérant est déjà assigné à une autre entreprise !");
            entreprise.setGerant(newGerant);
        }

        return toResponse(entrepriseRepository.save(entreprise));
    }

    public void delete(Long id) {
        if (!entrepriseRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entreprise non trouvée");
        entrepriseRepository.deleteById(id);
    }

    private EntrepriseResponse toResponse(Entreprise e) {
        return new EntrepriseResponse(
                e.getId(),
                e.getNom(),
                e.getAdresse(),
                e.getTelephone(),
                e.getSecteur().getId(),
                e.getSecteur().getNom(),
                e.getGerant().getId(),
                e.getGerant().getNom(),
                e.getGerant().getPrenom()
        );
    }
}