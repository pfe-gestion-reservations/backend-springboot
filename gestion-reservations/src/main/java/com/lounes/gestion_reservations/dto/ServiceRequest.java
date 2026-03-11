package com.lounes.gestion_reservations.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class ServiceRequest {

    @NotBlank
    private String nom;

    private String description;

    private Integer dureeMinutes;

    private Double tarif;

    private Long entrepriseId;

    private String typeService;

    // RESSOURCE_PARTAGEE uniquement — liste des ressources à créer dès la création du service
    // Au moins 1 obligatoire si typeService = RESSOURCE_PARTAGEE
    private List<RessourceInlineRequest> ressources;

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(Integer dureeMinutes) { this.dureeMinutes = dureeMinutes; }
    public Double getTarif() { return tarif; }
    public void setTarif(Double tarif) { this.tarif = tarif; }
    public Long getEntrepriseId() { return entrepriseId; }
    public void setEntrepriseId(Long entrepriseId) { this.entrepriseId = entrepriseId; }
    public String getTypeService() { return typeService; }
    public void setTypeService(String typeService) { this.typeService = typeService; }
    public List<RessourceInlineRequest> getRessources() { return ressources; }
    public void setRessources(List<RessourceInlineRequest> ressources) { this.ressources = ressources; }

    // Classe interne pour les ressources inline
    public static class RessourceInlineRequest {
        private String nom;
        private String description;
        private Integer capacite;

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Integer getCapacite() { return capacite; }
        public void setCapacite(Integer capacite) { this.capacite = capacite; }
    }
}