package com.lounes.gestion_reservations.dto;

import jakarta.validation.constraints.NotBlank;

public class SecteurRequest {
    @NotBlank
    private String nom;

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
}