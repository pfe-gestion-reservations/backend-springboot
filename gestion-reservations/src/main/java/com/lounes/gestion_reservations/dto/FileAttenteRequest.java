package com.lounes.gestion_reservations.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class FileAttenteRequest {

    @NotNull
    private Long clientId;

    // EMPLOYE_DEDIE / HYBRIDE
    private Long employeId;

    @NotNull
    private Long serviceId;

    // Pour RESSOURCE_PARTAGEE : créneau souhaité
    private LocalDateTime heureDebut;

    // Pour EMPLOYE_DEDIE / HYBRIDE : réservation existante
    private Long reservationId;

    public Long getClientId()                  { return clientId; }
    public void setClientId(Long id)           { this.clientId = id; }
    public Long getEmployeId()                 { return employeId; }
    public void setEmployeId(Long id)          { this.employeId = id; }
    public Long getServiceId()                 { return serviceId; }
    public void setServiceId(Long id)          { this.serviceId = id; }
    public LocalDateTime getHeureDebut()       { return heureDebut; }
    public void setHeureDebut(LocalDateTime h) { this.heureDebut = h; }
    public Long getReservationId()             { return reservationId; }
    public void setReservationId(Long id)      { this.reservationId = id; }
}