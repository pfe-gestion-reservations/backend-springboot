package com.lounes.gestion_reservations.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data

public class ServiceResponse {
    private Long id;
    private String nom;
    private String description;
    private Integer dureeMinutes;
    private Double tarif;
    private Boolean archived;
    private Long entrepriseId;

    public ServiceResponse() {
    }

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

    public Integer getDureeMinutes() {
        return dureeMinutes;
    }

    public void setDureeMinutes(Integer dureeMinutes) {
        this.dureeMinutes = dureeMinutes;
    }

    public Double getTarif() {
        return tarif;
    }

    public void setTarif(Double tarif) {
        this.tarif = tarif;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public Long getEntrepriseId() {
        return entrepriseId;
    }

    public void setEntrepriseId(Long entrepriseId) {
        this.entrepriseId = entrepriseId;
    }

    public ServiceResponse(Long id, String nom, String description, Integer dureeMinutes, Double tarif, Boolean archived, Long entrepriseId) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.dureeMinutes = dureeMinutes;
        this.tarif = tarif;
        this.archived = archived;
        this.entrepriseId = entrepriseId;
    }
}