package com.lounes.gestion_reservations.dto;

public class EmployeCheckResponse {
    private String statut;
    private String message;
    private Long userId;
    private String nom;
    private String prenom;
    private String email;

    public EmployeCheckResponse(String statut, String message, Long userId,
                                String nom, String prenom, String email) {
        this.statut = statut;
        this.message = message;
        this.userId = userId;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
    }

    public String getStatut() { return statut; }
    public String getMessage() { return message; }
    public Long getUserId() { return userId; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
}