package com.lounes.gestion_reservations.dto;

public class RessourceResponse {
    private Long id;
    private String nom;
    private String description;
    private Boolean archived;
    private Long serviceId;
    private String serviceNom;
    private Long entrepriseId;

    public RessourceResponse() {}

    public RessourceResponse(Long id, String nom, String description, Boolean archived,
                             Long serviceId, String serviceNom, Long entrepriseId) {
        this.id = id; this.nom = nom; this.description = description;
        this.archived = archived; this.serviceId = serviceId;
        this.serviceNom = serviceNom; this.entrepriseId = entrepriseId;
    }

    public Long getId()           { return id; }
    public String getNom()        { return nom; }
    public String getDescription(){ return description; }
    public Boolean getArchived()  { return archived; }
    public Long getServiceId()    { return serviceId; }
    public String getServiceNom() { return serviceNom; }
    public Long getEntrepriseId() { return entrepriseId; }
}