package com.lounes.gestion_reservations.dto;

import java.util.List;

public class ClientResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String numtel;
    private Boolean archived;
    private String createdBy;
    private List<EntrepriseInfo> entreprises; // NOUVEAU

    // Constructeur existant (sans entreprises — pour compatibilité)
    public ClientResponse(Long id, String nom, String prenom, String email,
                          String numtel, Boolean archived, String createdBy) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.numtel = numtel;
        this.archived = archived;
        this.createdBy = createdBy;
    }

    // Sous-classe pour les infos entreprise
    public static class EntrepriseInfo {
        private Long id;
        private String nom;
        private String secteur;

        public EntrepriseInfo(Long id, String nom, String secteur) {
            this.id = id;
            this.nom = nom;
            this.secteur = secteur;
        }

        public Long getId() { return id; }
        public String getNom() { return nom; }
        public String getSecteur() { return secteur; }
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getNumtel() { return numtel; }
    public Boolean getArchived() { return archived; }
    public String getCreatedBy() { return createdBy; }
    public List<EntrepriseInfo> getEntreprises() { return entreprises; }
    public void setEntreprises(List<EntrepriseInfo> entreprises) { this.entreprises = entreprises; }
}