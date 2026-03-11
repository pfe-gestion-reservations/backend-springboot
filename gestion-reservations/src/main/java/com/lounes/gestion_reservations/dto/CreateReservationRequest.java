package com.lounes.gestion_reservations.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateReservationRequest {
    private Long clientId;
    private Long serviceId;
    private Long employeId;      // nullable
    private Long ressourceId;    // nullable
    private LocalDateTime heureDebut;
    private Integer nombrePersonnes; // nullable → défaut 1
    private String notes;

    public CreateReservationRequest() {
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getEmployeId() {
        return employeId;
    }

    public void setEmployeId(Long employeId) {
        this.employeId = employeId;
    }

    public Long getRessourceId() {
        return ressourceId;
    }

    public void setRessourceId(Long ressourceId) {
        this.ressourceId = ressourceId;
    }

    public LocalDateTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalDateTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public Integer getNombrePersonnes() {
        return nombrePersonnes;
    }

    public void setNombrePersonnes(Integer nombrePersonnes) {
        this.nombrePersonnes = nombrePersonnes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public CreateReservationRequest(Long clientId, Long serviceId, Long employeId, Long ressourceId, LocalDateTime heureDebut, Integer nombrePersonnes, String notes) {
        this.clientId = clientId;
        this.serviceId = serviceId;
        this.employeId = employeId;
        this.ressourceId = ressourceId;
        this.heureDebut = heureDebut;
        this.nombrePersonnes = nombrePersonnes;
        this.notes = notes;
    }
}