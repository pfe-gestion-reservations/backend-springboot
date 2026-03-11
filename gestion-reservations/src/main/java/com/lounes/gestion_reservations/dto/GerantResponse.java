package com.lounes.gestion_reservations.dto;

public class GerantResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private Boolean archived;

    public GerantResponse() {}

    public GerantResponse(Long id, String nom, String prenom, String email, Boolean archived) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.archived = archived;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Boolean getArchived() { return archived; }
    public void setArchived(Boolean archived) { this.archived = archived; }
}