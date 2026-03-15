package com.lounes.gestion_reservations.dto;

import java.util.List;

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private List<String> roles;
    private Long entrepriseId;  // null si pas GERANT

    public JwtResponse(String token, Long id, String email, String nom, String prenom,
                       List<String> roles, Long entrepriseId) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.roles = roles;
        this.entrepriseId = entrepriseId;
    }

    public String getToken()       { return token; }
    public String getType()        { return type; }
    public Long getId()            { return id; }
    public String getEmail()       { return email; }
    public String getNom()         { return nom; }
    public String getPrenom()      { return prenom; }
    public List<String> getRoles() { return roles; }
    public Long getEntrepriseId()  { return entrepriseId; }
}