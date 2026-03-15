package com.lounes.gestion_reservations.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_attente")
public class FileAttente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    // Nullable — présent pour EMPLOYE_DEDIE / HYBRIDE
    @ManyToOne
    @JoinColumn(name = "employe_id", nullable = true)
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

    // Pour RESSOURCE_PARTAGEE : créneau demandé
    private LocalDateTime heureDebut;

    private LocalDateTime heureArrivee;

    @Enumerated(EnumType.STRING)
    private StatutFileAttente statut = StatutFileAttente.EN_ATTENTE;

    public FileAttente() {}

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }
    public Client getClient()                    { return client; }
    public void setClient(Client c)              { this.client = c; }
    public Employe getEmploye()                  { return employe; }
    public void setEmploye(Employe e)            { this.employe = e; }
    public ServiceEntity getService()            { return service; }
    public void setService(ServiceEntity s)      { this.service = s; }
    public Reservation getReservation()          { return reservation; }
    public void setReservation(Reservation r)    { this.reservation = r; }
    public Entreprise getEntreprise()            { return entreprise; }
    public void setEntreprise(Entreprise e)      { this.entreprise = e; }
    public LocalDateTime getHeureDebut()         { return heureDebut; }
    public void setHeureDebut(LocalDateTime h)   { this.heureDebut = h; }
    public LocalDateTime getHeureArrivee()       { return heureArrivee; }
    public void setHeureArrivee(LocalDateTime h) { this.heureArrivee = h; }
    public StatutFileAttente getStatut()         { return statut; }
    public void setStatut(StatutFileAttente s)   { this.statut = s; }
}