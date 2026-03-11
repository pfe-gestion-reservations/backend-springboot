package com.lounes.gestion_reservations.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ressources")
@Data
public class Ressource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom; // "Terrain 1", "Box A", "Chaise 3"

    private String description;

    @Column(nullable = false)
    private Integer capacite = 1; // nb personnes max sur cette ressource

    @Column(nullable = false)
    private Boolean archived = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    public Ressource() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCapacite() {
        return capacite;
    }

    public void setCapacite(Integer capacite) {
        this.capacite = capacite;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public ServiceEntity getService() {
        return service;
    }

    public void setService(ServiceEntity service) {
        this.service = service;
    }

    public Entreprise getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(Entreprise entreprise) {
        this.entreprise = entreprise;
    }

    public Ressource(Long id, String nom, String description, Integer capacite, Boolean archived, ServiceEntity service, Entreprise entreprise) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.capacite = capacite;
        this.archived = archived;
        this.service = service;
        this.entreprise = entreprise;
    }
}