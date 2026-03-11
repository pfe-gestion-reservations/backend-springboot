package com.lounes.gestion_reservations.dto;

import com.lounes.gestion_reservations.model.StatutFileAttente;
import java.time.LocalDateTime;

public class FileAttenteResponse {

    private Long id;
    private String clientNom;
    private String clientPrenom;
    private String employeNom;
    private String employePrenom;
    private String serviceNom;
    private LocalDateTime heureArrivee;
    private LocalDateTime dateHeureRdv;
    private StatutFileAttente statut;

    public FileAttenteResponse(Long id, String clientNom, String clientPrenom,
                               String employeNom, String employePrenom,
                               String serviceNom, LocalDateTime heureArrivee,
                               LocalDateTime dateHeureRdv, StatutFileAttente statut) {
        this.id = id;
        this.clientNom = clientNom;
        this.clientPrenom = clientPrenom;
        this.employeNom = employeNom;
        this.employePrenom = employePrenom;
        this.serviceNom = serviceNom;
        this.heureArrivee = heureArrivee;
        this.dateHeureRdv = dateHeureRdv;
        this.statut = statut;
    }

    public Long getId()                    { return id; }
    public String getClientNom()           { return clientNom; }
    public String getClientPrenom()        { return clientPrenom; }
    public String getEmployeNom()          { return employeNom; }
    public String getEmployePrenom()       { return employePrenom; }
    public String getServiceNom()          { return serviceNom; }
    public LocalDateTime getHeureArrivee() { return heureArrivee; }
    public LocalDateTime getDateHeureRdv() { return dateHeureRdv; }
    public StatutFileAttente getStatut()   { return statut; }
}