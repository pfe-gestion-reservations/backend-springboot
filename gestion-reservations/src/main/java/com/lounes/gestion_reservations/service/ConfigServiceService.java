package com.lounes.gestion_reservations.service;

import com.lounes.gestion_reservations.dto.ConfigServiceRequest;
import com.lounes.gestion_reservations.dto.ConfigServiceResponse;
import com.lounes.gestion_reservations.model.ConfigService;
import com.lounes.gestion_reservations.model.ServiceEntity;
import com.lounes.gestion_reservations.repo.ConfigServiceRepository;
import com.lounes.gestion_reservations.repo.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConfigServiceService {

    @Autowired private ConfigServiceRepository configServiceRepository;
    @Autowired private ServiceRepository serviceRepository;

    // ── CREATE ou UPDATE (upsert) ────────────────────────────
    // Un service ne peut avoir qu'une seule config → si elle existe, on la met à jour
    public ConfigServiceResponse save(ConfigServiceRequest request) {
        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service non trouvé"));

        ConfigService config = configServiceRepository
                .findByServiceId(request.getServiceId())
                .orElse(new ConfigService());

        config.setService(service);
        config.setTypeService(request.getTypeService());
        config.setDureeMinutes(request.getDureeMinutes());
        config.setCapaciteMinPersonnes(request.getCapaciteMinPersonnes());
        config.setCapaciteMaxPersonnes(request.getCapaciteMaxPersonnes());
        config.setRessourceObligatoire(
                request.getRessourceObligatoire() != null ? request.getRessourceObligatoire() : false);
        config.setEmployeObligatoire(
                request.getEmployeObligatoire() != null ? request.getEmployeObligatoire() : false);
        config.setReservationEnGroupe(
                request.getReservationEnGroupe() != null ? request.getReservationEnGroupe() : false);
        // RESSOURCE_PARTAGEE → file d'attente toujours active (règle métier)
        boolean fileAttenteActive = request.getTypeService() == com.lounes.gestion_reservations.model.TypeService.RESSOURCE_PARTAGEE
                ? true
                : (request.getFileAttenteActive() != null ? request.getFileAttenteActive() : true);
        config.setFileAttenteActive(fileAttenteActive);
        config.setAvanceReservationJours(request.getAvanceReservationJours());
        config.setAnnulationHeures(request.getAnnulationHeures());

        return toResponse(configServiceRepository.save(config));
    }

    // ── GET BY SERVICE ───────────────────────────────────────
    public ConfigServiceResponse getByService(Long serviceId) {
        ConfigService config = configServiceRepository.findByServiceId(serviceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Aucune configuration pour ce service"));
        return toResponse(config);
    }

    // ── DELETE ───────────────────────────────────────────────
    public void delete(Long serviceId) {
        ConfigService config = configServiceRepository.findByServiceId(serviceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Aucune configuration pour ce service"));
        configServiceRepository.delete(config);
    }

    // ── toResponse ───────────────────────────────────────────
    private ConfigServiceResponse toResponse(ConfigService c) {
        return new ConfigServiceResponse(
                c.getId(),
                c.getService().getId(),
                c.getService().getNom(),
                c.getTypeService(),
                c.getDureeMinutes(),
                c.getCapaciteMinPersonnes(),
                c.getCapaciteMaxPersonnes(),
                c.getRessourceObligatoire(),
                c.getEmployeObligatoire(),
                c.getReservationEnGroupe(),
                c.getFileAttenteActive(),
                c.getAvanceReservationJours(),
                c.getAnnulationHeures()
        );
    }
}