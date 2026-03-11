package com.lounes.gestion_reservations.dto;

import com.lounes.gestion_reservations.model.JourSemaine;
import lombok.Data;
import java.time.LocalTime;

@Data
public class CreateDisponibiliteRequest {
    private Long serviceId;
    private JourSemaine jour;
    private LocalTime heureDebut;
    private LocalTime heureFin;

    public CreateDisponibiliteRequest(Long serviceId, JourSemaine jour, LocalTime heureDebut, LocalTime heureFin) {
        this.serviceId = serviceId;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public JourSemaine getJour() {
        return jour;
    }

    public void setJour(JourSemaine jour) {
        this.jour = jour;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }
}