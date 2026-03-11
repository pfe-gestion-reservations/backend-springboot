package com.lounes.gestion_reservations.dto;

import com.lounes.gestion_reservations.model.JourSemaine;

import java.time.LocalTime;

public class DisponibiliteResponse {
    private Long id;
    private Long serviceId;
    private String serviceNom;
    private JourSemaine jour;
    private LocalTime heureDebut;
    private LocalTime heureFin;


    public DisponibiliteResponse(Long id, Long serviceId, String serviceNom, JourSemaine jour, LocalTime heureDebut, LocalTime heureFin) {
        this.id = id;
        this.serviceId = serviceId;
        this.serviceNom = serviceNom;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

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