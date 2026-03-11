package com.lounes.gestion_reservations.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data

public class RessourceResponse {
    private Long id;
    private String nom;
    private String description;
    private Integer capacite;
    private Boolean archived;
    private Long serviceId;
    private String serviceNom;
    private Long entrepriseId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
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

    public Long getEntrepriseId() {
        return entrepriseId;
    }

    public void setEntrepriseId(Long entrepriseId) {
        this.entrepriseId = entrepriseId;
    }

    public RessourceResponse() {
    }

    public RessourceResponse(Long id, String nom, String description, Integer capacite, Boolean archived, Long serviceId, String serviceNom, Long entrepriseId) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.capacite = capacite;
        this.archived = archived;
        this.serviceId = serviceId;
        this.serviceNom = serviceNom;
        this.entrepriseId = entrepriseId;
    }
}