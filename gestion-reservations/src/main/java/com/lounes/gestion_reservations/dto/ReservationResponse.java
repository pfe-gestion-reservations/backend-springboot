package com.lounes.gestion_reservations.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data

public class ReservationResponse {
    private Long id;
    private Long clientId;
    private String clientNom;
    private String clientPrenom;
    private Long employeId;
    private String employeNom;
    private String employePrenom;
    private Long serviceId;
    private String serviceNom;
    private Long ressourceId;
    private String ressourceNom;
    private LocalDateTime heureDebut;  // ← remplace dateHeure
    private LocalDateTime heureFin;    // ← nouveau
    private Integer nombrePersonnes;   // ← nouveau
    private Double prixTotal;
    private String statut;
    private String notes;

    public ReservationResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getClientNom() {
        return clientNom;
    }

    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }

    public String getClientPrenom() {
        return clientPrenom;
    }

    public void setClientPrenom(String clientPrenom) {
        this.clientPrenom = clientPrenom;
    }

    public Long getEmployeId() {
        return employeId;
    }

    public void setEmployeId(Long employeId) {
        this.employeId = employeId;
    }

    public String getEmployeNom() {
        return employeNom;
    }

    public void setEmployeNom(String employeNom) {
        this.employeNom = employeNom;
    }

    public String getEmployePrenom() {
        return employePrenom;
    }

    public void setEmployePrenom(String employePrenom) {
        this.employePrenom = employePrenom;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceNom() {
        return serviceNom;
    }

    public void setServiceNom(String serviceNom) {
        this.serviceNom = serviceNom;
    }

    public Long getRessourceId() {
        return ressourceId;
    }

    public void setRessourceId(Long ressourceId) {
        this.ressourceId = ressourceId;
    }

    public String getRessourceNom() {
        return ressourceNom;
    }

    public void setRessourceNom(String ressourceNom) {
        this.ressourceNom = ressourceNom;
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

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ReservationResponse(Long id, Long clientId, String clientNom, String clientPrenom, Long employeId, String employeNom, String employePrenom, Long serviceId, String serviceNom, Long ressourceId, String ressourceNom, LocalDateTime heureDebut, LocalDateTime heureFin, Integer nombrePersonnes, Double prixTotal, String statut, String notes) {
        this.id = id;
        this.clientId = clientId;
        this.clientNom = clientNom;
        this.clientPrenom = clientPrenom;
        this.employeId = employeId;
        this.employeNom = employeNom;
        this.employePrenom = employePrenom;
        this.serviceId = serviceId;
        this.serviceNom = serviceNom;
        this.ressourceId = ressourceId;
        this.ressourceNom = ressourceNom;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.nombrePersonnes = nombrePersonnes;
        this.prixTotal = prixTotal;
        this.statut = statut;
        this.notes = notes;
    }
}