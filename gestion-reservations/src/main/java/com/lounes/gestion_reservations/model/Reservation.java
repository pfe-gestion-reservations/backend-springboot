package com.lounes.gestion_reservations.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id")
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ressource_id")
    private Ressource ressource;

    @Column(nullable = false)
    private LocalDateTime heureDebut;

    @Column(nullable = false)
    private LocalDateTime heureFin;

    @Column(nullable = false)
    private Integer nombrePersonnes = 1;

    private Double prixTotal;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutReservation statut = StatutReservation.EN_ATTENTE;

    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private Boolean rappelEnvoye = false;

    public Reservation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
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

    public Employe getEmploye() {
        return employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
    }

    public Ressource getRessource() {
        return ressource;
    }

    public void setRessource(Ressource ressource) {
        this.ressource = ressource;
    }

    public LocalDateTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalDateTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalDateTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalDateTime heureFin) {
        this.heureFin = heureFin;
    }

    public Integer getNombrePersonnes() {
        return nombrePersonnes;
    }

    public void setNombrePersonnes(Integer nombrePersonnes) {
        this.nombrePersonnes = nombrePersonnes;
    }

    public Double getPrixTotal() {
        return prixTotal;
    }

    public void setPrixTotal(Double prixTotal) {
        this.prixTotal = prixTotal;
    }


    public StatutReservation getStatut() {
        return statut;
    }

    public void setStatut(StatutReservation statut) {
        this.statut = statut;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getRappelEnvoye() {
        return rappelEnvoye;
    }

    public void setRappelEnvoye(Boolean rappelEnvoye) {
        this.rappelEnvoye = rappelEnvoye;
    }

    public Reservation(Long id, Client client, ServiceEntity service, Entreprise entreprise, Employe employe, Ressource ressource, LocalDateTime heureDebut, LocalDateTime heureFin, Integer nombrePersonnes, Double prixTotal, StatutReservation statut, String notes, LocalDateTime createdAt, Boolean rappelEnvoye) {
        this.id = id;
        this.client = client;
        this.service = service;
        this.entreprise = entreprise;
        this.employe = employe;
        this.ressource = ressource;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.nombrePersonnes = nombrePersonnes;
        this.prixTotal = prixTotal;
        this.statut = statut;
        this.notes = notes;
        this.createdAt = createdAt;
        this.rappelEnvoye = rappelEnvoye;
    }


}