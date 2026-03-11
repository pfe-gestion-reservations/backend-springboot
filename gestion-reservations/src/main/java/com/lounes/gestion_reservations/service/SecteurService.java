package com.lounes.gestion_reservations.service;

import com.lounes.gestion_reservations.dto.SecteurRequest;
import com.lounes.gestion_reservations.dto.SecteurResponse;
import com.lounes.gestion_reservations.model.Secteur;
import com.lounes.gestion_reservations.repo.SecteurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SecteurService {

    @Autowired private SecteurRepository secteurRepository;

    public SecteurResponse create(SecteurRequest request) {
        if (secteurRepository.existsByNom(request.getNom()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Secteur déjà existant !");

        Secteur secteur = new Secteur();
        secteur.setNom(request.getNom());
        return toResponse(secteurRepository.save(secteur));
    }

    public List<SecteurResponse> getAll() {
        return secteurRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public SecteurResponse getById(Long id) {
        return toResponse(secteurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Secteur non trouvé")));
    }

    public SecteurResponse update(Long id, SecteurRequest request) {
        Secteur secteur = secteurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Secteur non trouvé"));
        secteur.setNom(request.getNom());
        return toResponse(secteurRepository.save(secteur));
    }

    public void delete(Long id) {
        if (!secteurRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Secteur non trouvé");
        secteurRepository.deleteById(id);
    }

    private SecteurResponse toResponse(Secteur secteur) {
        return new SecteurResponse(secteur.getId(), secteur.getNom());
    }
}