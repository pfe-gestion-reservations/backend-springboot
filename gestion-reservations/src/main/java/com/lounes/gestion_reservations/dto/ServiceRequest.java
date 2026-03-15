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

    // Config RESSOURCE_PARTAGEE
    private Integer capaciteMinPersonnes;
    private Integer capaciteMaxPersonnes;
    private Integer annulationHeures;
    private Integer avanceReservationJours;

    // Ressources inline (RESSOURCE_PARTAGEE uniquement)
    private List<RessourceInlineRequest> ressources;

    public String getNom()                          { return nom; }
    public void setNom(String nom)                  { this.nom = nom; }
    public String getDescription()                  { return description; }
    public void setDescription(String d)            { this.description = d; }
    public Integer getDureeMinutes()                { return dureeMinutes; }
    public void setDureeMinutes(Integer d)          { this.dureeMinutes = d; }
    public Double getTarif()                        { return tarif; }
    public void setTarif(Double tarif)              { this.tarif = tarif; }
    public Long getEntrepriseId()                   { return entrepriseId; }
    public void setEntrepriseId(Long id)            { this.entrepriseId = id; }
    public String getTypeService()                  { return typeService; }
    public void setTypeService(String t)            { this.typeService = t; }
    public Integer getCapaciteMinPersonnes()        { return capaciteMinPersonnes; }
    public void setCapaciteMinPersonnes(Integer c)  { this.capaciteMinPersonnes = c; }
    public Integer getCapaciteMaxPersonnes()        { return capaciteMaxPersonnes; }
    public void setCapaciteMaxPersonnes(Integer c)  { this.capaciteMaxPersonnes = c; }
    public Integer getAnnulationHeures()            { return annulationHeures; }
    public void setAnnulationHeures(Integer a)      { this.annulationHeures = a; }
    public Integer getAvanceReservationJours()      { return avanceReservationJours; }
    public void setAvanceReservationJours(Integer a){ this.avanceReservationJours = a; }
    public List<RessourceInlineRequest> getRessources()          { return ressources; }
    public void setRessources(List<RessourceInlineRequest> r)    { this.ressources = r; }

    public static class RessourceInlineRequest {
        private String nom;
        private String description;
        private Integer capacite;

        public String getNom()             { return nom; }
        public void setNom(String nom)     { this.nom = nom; }
        public String getDescription()     { return description; }
        public void setDescription(String d){ this.description = d; }
        public Integer getCapacite()       { return capacite; }
        public void setCapacite(Integer c) { this.capacite = c; }
    }
}