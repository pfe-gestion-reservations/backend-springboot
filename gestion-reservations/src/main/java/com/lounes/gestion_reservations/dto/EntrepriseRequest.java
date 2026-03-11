package com.lounes.gestion_reservations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EntrepriseRequest {
    @NotBlank
    private String nom;
    @NotBlank
    private String adresse;
    @NotBlank
    private String telephone;
    @NotNull
    private Long secteurId;
    @NotNull
    private Long gerantId;

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public Long getSecteurId() { return secteurId; }
    public void setSecteurId(Long secteurId) { this.secteurId = secteurId; }
    public Long getGerantId() { return gerantId; }
    public void setGerantId(Long gerantId) { this.gerantId = gerantId; }
}