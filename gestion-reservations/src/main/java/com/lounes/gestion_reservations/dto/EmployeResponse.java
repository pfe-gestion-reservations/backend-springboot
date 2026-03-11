package com.lounes.gestion_reservations.dto;

public class EmployeResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String specialite;
        private Boolean archived;

    public EmployeResponse(Long id, String nom, String prenom,
                           String email, String specialite, Boolean archived) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.specialite = specialite;
        this.archived = archived;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getSpecialite() { return specialite; }
    public Boolean getArchived() { return archived; }
}