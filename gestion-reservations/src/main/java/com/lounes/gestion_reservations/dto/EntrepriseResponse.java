package com.lounes.gestion_reservations.dto;

public class EntrepriseResponse {
    private Long id;
    private String nom;
    private String adresse;
    private String telephone;
    private Long secteurId;
    private String secteurNom;
    private Long gerantId;
    private String gerantNom;
    private String gerantPrenom;

    public EntrepriseResponse(Long id, String nom, String adresse, String telephone,
                              Long secteurId, String secteurNom,
                              Long gerantId, String gerantNom, String gerantPrenom) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.secteurId = secteurId;
        this.secteurNom = secteurNom;
        this.gerantId = gerantId;
        this.gerantNom = gerantNom;
        this.gerantPrenom = gerantPrenom;
    }

    public EntrepriseResponse() {
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getAdresse() { return adresse; }
    public String getTelephone() { return telephone; }
    public Long getSecteurId() { return secteurId; }
    public String getSecteurNom() { return secteurNom; }
    public Long getGerantId() { return gerantId; }
    public String getGerantNom() { return gerantNom; }
    public String getGerantPrenom() { return gerantPrenom; }
}