package com.lounes.gestion_reservations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RessourceRequest {

    @NotBlank
    private String nom;

    private String description;

    @NotNull
    private Long serviceId;

    public String getNom()              { return nom; }
    public void setNom(String nom)      { this.nom = nom; }
    public String getDescription()      { return description; }
    public void setDescription(String d){ this.description = d; }
    public Long getServiceId()          { return serviceId; }
    public void setServiceId(Long id)   { this.serviceId = id; }
}