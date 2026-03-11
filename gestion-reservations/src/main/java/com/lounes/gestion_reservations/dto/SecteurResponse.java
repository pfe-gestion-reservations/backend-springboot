package com.lounes.gestion_reservations.dto;

public class SecteurResponse {
    private Long id;
    private String nom;

    public SecteurResponse(Long id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
}