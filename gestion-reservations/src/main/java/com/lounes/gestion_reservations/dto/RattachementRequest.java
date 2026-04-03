package com.lounes.gestion_reservations.dto;

public class RattachementRequest {
    private Long userId;
    private Long entrepriseId;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getEntrepriseId() { return entrepriseId; }
    public void setEntrepriseId(Long entrepriseId) { this.entrepriseId = entrepriseId; }


    public RattachementRequest() {
    }

    public RattachementRequest(Long userId, Long entrepriseId) {
        this.userId = userId;
        this.entrepriseId = entrepriseId;

    }
}