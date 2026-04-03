package com.lounes.gestion_reservations.dto;

public class EmployeResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private Boolean archived;
    private Long entrepriseId;      //null si libre
    private String entrepriseNom;

    public EmployeResponse(Long id, String nom, String prenom,
                           String email, Boolean archived,
                           Long entrepriseId, String entrepriseNom) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.archived = archived;
        this.entrepriseId = entrepriseId;
        this.entrepriseNom = entrepriseNom;
    }

    public Long getId()             { return id; }
    public String getNom()          { return nom; }
    public String getPrenom()       { return prenom; }
    public String getEmail()        { return email; }
    public Boolean getArchived()    { return archived; }
    public Long getEntrepriseId()   { return entrepriseId; }
    public String getEntrepriseNom(){ return entrepriseNom; }
}