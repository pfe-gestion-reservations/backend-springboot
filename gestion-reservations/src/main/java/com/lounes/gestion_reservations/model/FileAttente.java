package com.lounes.gestion_reservations.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "file_attente")
public class FileAttente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    private LocalDateTime heureArrivee;

    @Enumerated(EnumType.STRING)
    private StatutFileAttente statut = StatutFileAttente.EN_ATTENTE;

    public FileAttente(Long id, Client client, Employe employe, ServiceEntity service, Reservation reservation, Entreprise entreprise, LocalDateTime heureArrivee, StatutFileAttente statut) {
        this.id = id;
        this.client = client;
        this.employe = employe;
        this.service = service;
        this.reservation = reservation;
        this.entreprise = entreprise;
        this.heureArrivee = heureArrivee;
        this.statut = statut;
    }

    public FileAttente() {
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

    public Employe getEmploye() {
        return employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
    }

    public ServiceEntity getService() {
        return service;
    }

    public void setService(ServiceEntity service) {
        this.service = service;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public Entreprise getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(Entreprise entreprise) {
        this.entreprise = entreprise;
    }

    public LocalDateTime getHeureArrivee() {
        return heureArrivee;
    }

    public void setHeureArrivee(LocalDateTime heureArrivee) {
        this.heureArrivee = heureArrivee;
    }

    public StatutFileAttente getStatut() {
        return statut;
    }

    public void setStatut(StatutFileAttente statut) {
        this.statut = statut;
    }
}