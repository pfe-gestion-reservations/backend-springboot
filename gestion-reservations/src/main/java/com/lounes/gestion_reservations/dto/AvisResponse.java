package com.lounes.gestion_reservations.dto;

import java.time.LocalDateTime;

public class AvisResponse {
    private Long id;
    private String clientNom;
    private String clientPrenom;
    private String serviceNom;
    private Integer note;
    private String commentaire;
    private LocalDateTime dateAvis;
    private Long reservationId;

    public AvisResponse(Long id, String clientNom, String clientPrenom,
                        String serviceNom, Integer note,
                        String commentaire, LocalDateTime dateAvis,
                        Long reservationId) {
        this.id = id;
        this.clientNom = clientNom;
        this.clientPrenom = clientPrenom;
        this.serviceNom = serviceNom;
        this.note = note;
        this.commentaire = commentaire;
        this.dateAvis = dateAvis;
        this.reservationId = reservationId;
    }

    public Long getId()             { return id; }
    public String getClientNom()    { return clientNom; }
    public String getClientPrenom() { return clientPrenom; }
    public String getServiceNom()   { return serviceNom; }
    public Integer getNote()        { return note; }
    public String getCommentaire()  { return commentaire; }
    public LocalDateTime getDateAvis() { return dateAvis; }
    public Long getReservationId()  { return reservationId; }
}