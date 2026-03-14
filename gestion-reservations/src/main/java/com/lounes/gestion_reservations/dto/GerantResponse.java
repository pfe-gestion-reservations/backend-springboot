package com.lounes.gestion_reservations.dto;

public class GerantResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private Boolean archived;

    // Entreprise associée (null si libre)
    private Long entrepriseId;
    private String entrepriseNom;
    private String entrepriseAdresse;
    private String entrepriseTelephone;
    private String entrepriseSecteur;

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

    public Long getEntrepriseId() { return entrepriseId; }
    public void setEntrepriseId(Long entrepriseId) { this.entrepriseId = entrepriseId; }

    public String getEntrepriseNom() { return entrepriseNom; }
    public void setEntrepriseNom(String entrepriseNom) { this.entrepriseNom = entrepriseNom; }

    public String getEntrepriseAdresse() { return entrepriseAdresse; }
    public void setEntrepriseAdresse(String entrepriseAdresse) { this.entrepriseAdresse = entrepriseAdresse; }

    public String getEntrepriseTelephone() { return entrepriseTelephone; }
    public void setEntrepriseTelephone(String entrepriseTelephone) { this.entrepriseTelephone = entrepriseTelephone; }

    public String getEntrepriseSecteur() { return entrepriseSecteur; }
    public void setEntrepriseSecteur(String entrepriseSecteur) { this.entrepriseSecteur = entrepriseSecteur; }
}