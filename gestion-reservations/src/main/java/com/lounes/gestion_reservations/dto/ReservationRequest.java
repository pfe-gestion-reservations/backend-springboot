package com.lounes.gestion_reservations.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ReservationRequest {

    @NotNull
    private Long clientId;

    @NotNull
    private Long employeId;

    @NotNull
    private Long serviceId;

    @NotNull
    private LocalDateTime dateHeure;

    private String notes;

    public ReservationRequest() {
    }

    public ReservationRequest(Long clientId, Long employeId, Long serviceId, LocalDateTime dateHeure, String notes) {
        this.clientId = clientId;
        this.employeId = employeId;
        this.serviceId = serviceId;
        this.dateHeure = dateHeure;
        this.notes = notes;
    }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getEmployeId() { return employeId; }
    public void setEmployeId(Long employeId) { this.employeId = employeId; }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}