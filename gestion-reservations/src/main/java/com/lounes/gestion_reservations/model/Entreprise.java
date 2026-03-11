package com.lounes.gestion_reservations.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "entreprise")
public class Entreprise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String adresse;
    private String telephone;

    @ManyToOne
    @JoinColumn(name = "secteur_id")
    private Secteur secteur;

    @OneToOne
    @JoinColumn(name = "gerant_id", unique = true)
    private User gerant;

    public Entreprise(Long id, String nom, String adresse, String telephone, Secteur secteur, User gerant) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.secteur = secteur;
        this.gerant = gerant;
    }

    public Entreprise() {
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

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Secteur getSecteur() {
        return secteur;
    }

    public void setSecteur(Secteur secteur) {
        this.secteur = secteur;
    }

    public User getGerant() {
        return gerant;
    }

    public void setGerant(User gerant) {
        this.gerant = gerant;
    }
}