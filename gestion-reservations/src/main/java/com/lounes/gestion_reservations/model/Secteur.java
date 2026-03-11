package com.lounes.gestion_reservations.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "secteur")
public class Secteur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;

    public Secteur(Long id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    public Secteur() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}