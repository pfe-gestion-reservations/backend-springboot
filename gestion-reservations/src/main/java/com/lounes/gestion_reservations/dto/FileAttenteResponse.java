package com.lounes.gestion_reservations.dto;

import com.lounes.gestion_reservations.model.StatutFileAttente;
import java.time.LocalDateTime;

public class FileAttenteResponse {

    private Long id;
    private String clientNom;
    private String clientPrenom;
    private Long clientId;
    private String employeNom;
    private String employePrenom;
    private Long employeId;
    private String serviceNom;
    private Long serviceId;
    private String entrepriseNom;
    private Long entrepriseId;
    private Long reservationId;
    private String ressourceNom;
    private LocalDateTime heureDebut;
    private LocalDateTime heureArrivee;
    private LocalDateTime dateHeureRdv;
    private StatutFileAttente statut;

    public FileAttenteResponse() {}

    public FileAttenteResponse(Long id, String clientNom, String clientPrenom, Long clientId,
                               String employeNom, String employePrenom, Long employeId,
                               String serviceNom, Long serviceId,
                               String entrepriseNom, Long entrepriseId,
                               Long reservationId,
                               String ressourceNom,
                               LocalDateTime heureDebut, LocalDateTime heureArrivee,
                               LocalDateTime dateHeureRdv, StatutFileAttente statut) {
        this.id = id;
        this.clientNom = clientNom; this.clientPrenom = clientPrenom; this.clientId = clientId;
        this.employeNom = employeNom; this.employePrenom = employePrenom; this.employeId = employeId;
        this.serviceNom = serviceNom; this.serviceId = serviceId;
        this.entrepriseNom = entrepriseNom; this.entrepriseId = entrepriseId;
        this.reservationId = reservationId;
        this.ressourceNom = ressourceNom;
        this.heureDebut = heureDebut; this.heureArrivee = heureArrivee;
        this.dateHeureRdv = dateHeureRdv; this.statut = statut;
    }

    public Long getId()                    { return id; }
    public String getClientNom()           { return clientNom; }
    public String getClientPrenom()        { return clientPrenom; }
    public Long getClientId()              { return clientId; }
    public String getEmployeNom()          { return employeNom; }
    public String getEmployePrenom()       { return employePrenom; }
    public Long getEmployeId()             { return employeId; }
    public String getServiceNom()          { return serviceNom; }
    public Long getServiceId()             { return serviceId; }
    public String getEntrepriseNom()       { return entrepriseNom; }
    public Long getEntrepriseId()          { return entrepriseId; }
    public Long getReservationId()         { return reservationId; }
    public String getRessourceNom()        { return ressourceNom; }
    public LocalDateTime getHeureDebut()   { return heureDebut; }
    public LocalDateTime getHeureArrivee() { return heureArrivee; }
    public LocalDateTime getDateHeureRdv() { return dateHeureRdv; }
    public StatutFileAttente getStatut()   { return statut; }
}