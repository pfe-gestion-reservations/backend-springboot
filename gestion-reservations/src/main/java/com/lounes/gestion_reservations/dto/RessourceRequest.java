package com.lounes.gestion_reservations.dto;

import lombok.Data;

@Data
public class RessourceRequest {
    private String nom;
    private String description;
    private Integer capacite; // nullable → défaut 1
    private Long serviceId;

    public RessourceRequest() {
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCapacite() {
        return capacite;
    }

    public void setCapacite(Integer capacite) {
        this.capacite = capacite;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public RessourceRequest(String nom, String description, Integer capacite, Long serviceId) {
        this.nom = nom;
        this.description = description;
        this.capacite = capacite;
        this.serviceId = serviceId;
    }
}