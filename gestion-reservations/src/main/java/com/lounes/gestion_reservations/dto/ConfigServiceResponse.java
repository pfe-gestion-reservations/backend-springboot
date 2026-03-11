package com.lounes.gestion_reservations.dto;

import com.lounes.gestion_reservations.model.TypeService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data

public class ConfigServiceResponse {
    private Long id;
    private Long serviceId;
    private String serviceNom;
    private TypeService typeService;
    private Integer dureeMinutes;
    private Integer capaciteMinPersonnes;
    private Integer capaciteMaxPersonnes;
    private Boolean ressourceObligatoire;
    private Boolean employeObligatoire;
    private Boolean reservationEnGroupe;
    private Boolean fileAttenteActive;
    private Integer avanceReservationJours;
    private Integer annulationHeures;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceNom() {
        return serviceNom;
    }

    public void setServiceNom(String serviceNom) {
        this.serviceNom = serviceNom;
    }

    public TypeService getTypeService() {
        return typeService;
    }

    public void setTypeService(TypeService typeService) {
        this.typeService = typeService;
    }

    public Integer getDureeMinutes() {
        return dureeMinutes;
    }

    public void setDureeMinutes(Integer dureeMinutes) {
        this.dureeMinutes = dureeMinutes;
    }

    public Integer getCapaciteMinPersonnes() {
        return capaciteMinPersonnes;
    }

    public void setCapaciteMinPersonnes(Integer capaciteMinPersonnes) {
        this.capaciteMinPersonnes = capaciteMinPersonnes;
    }

    public Integer getCapaciteMaxPersonnes() {
        return capaciteMaxPersonnes;
    }

    public void setCapaciteMaxPersonnes(Integer capaciteMaxPersonnes) {
        this.capaciteMaxPersonnes = capaciteMaxPersonnes;
    }

    public Boolean getRessourceObligatoire() {
        return ressourceObligatoire;
    }

    public void setRessourceObligatoire(Boolean ressourceObligatoire) {
        this.ressourceObligatoire = ressourceObligatoire;
    }

    public Boolean getEmployeObligatoire() {
        return employeObligatoire;
    }

    public void setEmployeObligatoire(Boolean employeObligatoire) {
        this.employeObligatoire = employeObligatoire;
    }

    public Boolean getReservationEnGroupe() {
        return reservationEnGroupe;
    }

    public void setReservationEnGroupe(Boolean reservationEnGroupe) {
        this.reservationEnGroupe = reservationEnGroupe;
    }

    public Boolean getFileAttenteActive() {
        return fileAttenteActive;
    }

    public void setFileAttenteActive(Boolean fileAttenteActive) {
        this.fileAttenteActive = fileAttenteActive;
    }

    public Integer getAvanceReservationJours() {
        return avanceReservationJours;
    }

    public void setAvanceReservationJours(Integer avanceReservationJours) {
        this.avanceReservationJours = avanceReservationJours;
    }

    public Integer getAnnulationHeures() {
        return annulationHeures;
    }

    public void setAnnulationHeures(Integer annulationHeures) {
        this.annulationHeures = annulationHeures;
    }

    public ConfigServiceResponse() {
    }

    public ConfigServiceResponse(Long id, Long serviceId, String serviceNom, TypeService typeService, Integer dureeMinutes, Integer capaciteMinPersonnes, Integer capaciteMaxPersonnes, Boolean ressourceObligatoire, Boolean employeObligatoire, Boolean reservationEnGroupe, Boolean fileAttenteActive, Integer avanceReservationJours, Integer annulationHeures) {
        this.id = id;
        this.serviceId = serviceId;
        this.serviceNom = serviceNom;
        this.typeService = typeService;
        this.dureeMinutes = dureeMinutes;
        this.capaciteMinPersonnes = capaciteMinPersonnes;
        this.capaciteMaxPersonnes = capaciteMaxPersonnes;
        this.ressourceObligatoire = ressourceObligatoire;
        this.employeObligatoire = employeObligatoire;
        this.reservationEnGroupe = reservationEnGroupe;
        this.fileAttenteActive = fileAttenteActive;
        this.avanceReservationJours = avanceReservationJours;
        this.annulationHeures = annulationHeures;
    }
}