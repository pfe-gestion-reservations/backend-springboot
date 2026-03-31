package com.lounes.gestion_reservations.dto;

import com.lounes.gestion_reservations.model.TypeService;

public class ConfigServiceResponse {
    private Long id;
    private Long serviceId;
    private String serviceNom;
    private TypeService typeService;
    private Integer dureeMinutes;
    private Integer capaciteMinPersonnes;
    private Integer capaciteMaxPersonnes;
    private Boolean ressourceObligatoire;
    private Boolean employeObligatoire;
    private Boolean reservationEnGroupe;
    private Boolean fileAttenteActive;
    private Integer avanceReservationJours;
    private Integer annulationHeures;
    private Boolean tarifParPersonne;

    public ConfigServiceResponse() {}

    public Long getId()                             { return id; }
    public void setId(Long id)                      { this.id = id; }
    public Long getServiceId()                      { return serviceId; }
    public void setServiceId(Long serviceId)        { this.serviceId = serviceId; }
    public String getServiceNom()                   { return serviceNom; }
    public void setServiceNom(String s)             { this.serviceNom = s; }
    public TypeService getTypeService()             { return typeService; }
    public void setTypeService(TypeService t)       { this.typeService = t; }
    public Integer getDureeMinutes()                { return dureeMinutes; }
    public void setDureeMinutes(Integer d)          { this.dureeMinutes = d; }
    public Integer getCapaciteMinPersonnes()        { return capaciteMinPersonnes; }
    public void setCapaciteMinPersonnes(Integer c)  { this.capaciteMinPersonnes = c; }
    public Integer getCapaciteMaxPersonnes()        { return capaciteMaxPersonnes; }
    public void setCapaciteMaxPersonnes(Integer c)  { this.capaciteMaxPersonnes = c; }
    public Boolean getRessourceObligatoire()        { return ressourceObligatoire; }
    public void setRessourceObligatoire(Boolean b)  { this.ressourceObligatoire = b; }
    public Boolean getEmployeObligatoire()          { return employeObligatoire; }
    public void setEmployeObligatoire(Boolean b)    { this.employeObligatoire = b; }
    public Boolean getReservationEnGroupe()         { return reservationEnGroupe; }
    public void setReservationEnGroupe(Boolean b)   { this.reservationEnGroupe = b; }
    public Boolean getFileAttenteActive()           { return fileAttenteActive; }
    public void setFileAttenteActive(Boolean b)     { this.fileAttenteActive = b; }
    public Integer getAvanceReservationJours()      { return avanceReservationJours; }
    public void setAvanceReservationJours(Integer a){ this.avanceReservationJours = a; }
    public Integer getAnnulationHeures()            { return annulationHeures; }
    public void setAnnulationHeures(Integer a)      { this.annulationHeures = a; }
    public Boolean getTarifParPersonne()            { return tarifParPersonne; }
    public void setTarifParPersonne(Boolean t)      { this.tarifParPersonne = t; }
}