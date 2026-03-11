package com.lounes.gestion_reservations.dto;

public class RattachementRequest {
    private Long userId;
    private Long entrepriseId;
    private String specialite;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getEntrepriseId() { return entrepriseId; }
    public void setEntrepriseId(Long entrepriseId) { this.entrepriseId = entrepriseId; }
    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }


    public RattachementRequest() {
    }

    public RattachementRequest(Long userId, Long entrepriseId, String specialite) {
        this.userId = userId;
        this.entrepriseId = entrepriseId;
        this.specialite = specialite;
    }
}