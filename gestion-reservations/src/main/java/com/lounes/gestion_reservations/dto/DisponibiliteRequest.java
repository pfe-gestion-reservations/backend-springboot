package com.lounes.gestion_reservations.dto;

import com.lounes.gestion_reservations.model.JourSemaine;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public class DisponibiliteRequest {

    @NotNull
    private Long serviceId;
    @NotNull
    private JourSemaine jour;
    @NotNull
    private LocalTime heureDebut;
    @NotNull
    private LocalTime heureFin;

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public JourSemaine getJour() { return jour; }
    public void setJour(JourSemaine jour) { this.jour = jour; }
    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }
    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }
}