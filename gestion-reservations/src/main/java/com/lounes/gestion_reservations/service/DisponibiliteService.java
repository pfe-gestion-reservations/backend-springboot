package com.lounes.gestion_reservations.service;

import com.lounes.gestion_reservations.dto.DisponibiliteRequest;
import com.lounes.gestion_reservations.dto.DisponibiliteResponse;
import com.lounes.gestion_reservations.model.Disponibilite;
import com.lounes.gestion_reservations.model.ServiceEntity;
import com.lounes.gestion_reservations.repo.DisponibiliteRepository;
import com.lounes.gestion_reservations.repo.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DisponibiliteService {

    @Autowired private DisponibiliteRepository disponibiliteRepository;
    @Autowired private ServiceRepository serviceRepository;

    public DisponibiliteResponse create(DisponibiliteRequest request) {
        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service non trouvé"));

        Disponibilite dispo = new Disponibilite();
        dispo.setService(service);
        dispo.setJour(request.getJour());
        dispo.setHeureDebut(request.getHeureDebut());
        dispo.setHeureFin(request.getHeureFin());

        return toResponse(disponibiliteRepository.save(dispo));
    }

    public List<DisponibiliteResponse> getByService(Long serviceId) {
        if (!serviceRepository.existsById(serviceId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service non trouvé");
        return disponibiliteRepository.findByServiceId(serviceId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public DisponibiliteResponse update(Long id, DisponibiliteRequest request) {
        Disponibilite dispo = disponibiliteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disponibilité non trouvée"));

        if (request.getServiceId() != null &&
                !request.getServiceId().equals(dispo.getService().getId())) {
            ServiceEntity service = serviceRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service non trouvé"));
            dispo.setService(service);
        }

        dispo.setJour(request.getJour());
        dispo.setHeureDebut(request.getHeureDebut());
        dispo.setHeureFin(request.getHeureFin());

        return toResponse(disponibiliteRepository.save(dispo));
    }

    public void delete(Long id) {
        if (!disponibiliteRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Disponibilité non trouvée");
        disponibiliteRepository.deleteById(id);
    }

    private DisponibiliteResponse toResponse(Disponibilite d) {
        return new DisponibiliteResponse(
                d.getId(),
                d.getService().getId(),
                d.getService().getNom(),
                d.getJour(),
                d.getHeureDebut(),
                d.getHeureFin()
        );
    }
}