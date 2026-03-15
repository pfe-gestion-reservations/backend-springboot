package com.lounes.gestion_reservations.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ressources")
public class Ressource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String description;

    @Column(nullable = false)
    private Boolean archived = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    public Ressource() {}

    public Long getId()                      { return id; }
    public void setId(Long id)               { this.id = id; }
    public String getNom()                   { return nom; }
    public void setNom(String nom)           { this.nom = nom; }
    public String getDescription()           { return description; }
    public void setDescription(String d)     { this.description = d; }
    public Boolean getArchived()             { return archived; }
    public void setArchived(Boolean a)       { this.archived = a; }
    public ServiceEntity getService()        { return service; }
    public void setService(ServiceEntity s)  { this.service = s; }
    public Entreprise getEntreprise()        { return entreprise; }
    public void setEntreprise(Entreprise e)  { this.entreprise = e; }

    public Ressource(Long id, String nom, String description, Boolean archived,
                     ServiceEntity service, Entreprise entreprise) {
        this.id = id; this.nom = nom; this.description = description;
        this.archived = archived; this.service = service; this.entreprise = entreprise;
    }
}