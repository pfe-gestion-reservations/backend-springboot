package com.lounes.gestion_reservations.service;

import com.lounes.gestion_reservations.dto.ConfigServiceRequest;
import com.lounes.gestion_reservations.dto.ConfigServiceResponse;
import com.lounes.gestion_reservations.model.ConfigService;
import com.lounes.gestion_reservations.model.ServiceEntity;
import com.lounes.gestion_reservations.model.TypeService;
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

    public ConfigServiceResponse save(ConfigServiceRequest request) {
        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service non trouvé"));

        ConfigService config = configServiceRepository
                .findByServiceId(request.getServiceId())
                .orElseGet(ConfigService::new);

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
        boolean fileAttenteActive = request.getTypeService() == TypeService.RESSOURCE_PARTAGEE
                ? true
                : (request.getFileAttenteActive() != null ? request.getFileAttenteActive() : true);
        config.setFileAttenteActive(fileAttenteActive);
        config.setAvanceReservationJours(request.getAvanceReservationJours());
        config.setAnnulationHeures(request.getAnnulationHeures());
        config.setTarifParPersonne(
                request.getTarifParPersonne() != null ? request.getTarifParPersonne() : false);

        return toResponse(configServiceRepository.save(config));
    }

    public ConfigServiceResponse getByService(Long serviceId) {
        ConfigService config = configServiceRepository.findByServiceId(serviceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Aucune configuration pour ce service"));
        return toResponse(config);
    }

    public void delete(Long serviceId) {
        ConfigService config = configServiceRepository.findByServiceId(serviceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Aucune configuration pour ce service"));
        configServiceRepository.delete(config);
    }

    private ConfigServiceResponse toResponse(ConfigService c) {
        ConfigServiceResponse r = new ConfigServiceResponse();
        r.setId(c.getId());
        r.setServiceId(c.getService().getId());
        r.setServiceNom(c.getService().getNom());
        r.setTypeService(c.getTypeService());
        r.setDureeMinutes(c.getDureeMinutes());
        r.setCapaciteMinPersonnes(c.getCapaciteMinPersonnes());
        r.setCapaciteMaxPersonnes(c.getCapaciteMaxPersonnes());
        r.setRessourceObligatoire(c.getRessourceObligatoire());
        r.setEmployeObligatoire(c.getEmployeObligatoire());
        r.setReservationEnGroupe(c.getReservationEnGroupe());
        r.setFileAttenteActive(c.getFileAttenteActive());
        r.setAvanceReservationJours(c.getAvanceReservationJours());
        r.setAnnulationHeures(c.getAnnulationHeures());
        r.setTarifParPersonne(c.getTarifParPersonne() != null ? c.getTarifParPersonne() : false);
        return r;
    }
}