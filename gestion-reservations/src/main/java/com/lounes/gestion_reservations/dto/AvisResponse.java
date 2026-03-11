package com.lounes.gestion_reservations.dto;

import java.time.LocalDateTime;

public class AvisResponse {
    private Long id;
    private String clientNom;
    private String clientPrenom;
    private String employeNom;
    private String employePrenom;
    private String serviceNom;
    private Integer note;
    private String commentaire;
    private LocalDateTime dateAvis;

    public AvisResponse(Long id, String clientNom, String clientPrenom,
                        String employeNom, String employePrenom,
                        String serviceNom, Integer note,
                        String commentaire, LocalDateTime dateAvis) {
        this.id = id;
        this.clientNom = clientNom;
        this.clientPrenom = clientPrenom;
        this.employeNom = employeNom;
        this.employePrenom = employePrenom;
        this.serviceNom = serviceNom;
        this.note = note;
        this.commentaire = commentaire;
        this.dateAvis = dateAvis;
    }

    public Long getId() { return id; }
    public String getClientNom() { return clientNom; }
    public String getClientPrenom() { return clientPrenom; }
    public String getEmployeNom() { return employeNom; }
    public String getEmployePrenom() { return employePrenom; }
    public String getServiceNom() { return serviceNom; }
    public Integer getNote() { return note; }
    public String getCommentaire() { return commentaire; }
    public LocalDateTime getDateAvis() { return dateAvis; }
}