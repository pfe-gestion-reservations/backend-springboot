package com.lounes.gestion_reservations.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "config_services")
@Data
public class ConfigService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false, unique = true)
    private ServiceEntity service;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeService typeService;

    private Integer dureeMinutes;
    private Integer capaciteMinPersonnes;
    private Integer capaciteMaxPersonnes;

    @Column(nullable = false)
    private Boolean ressourceObligatoire = false;

    @Column(nullable = false)
    private Boolean employeObligatoire = false;

    @Column(nullable = false)
    private Boolean reservationEnGroupe = false;

    @Column(nullable = false)
    private Boolean fileAttenteActive = true;

    private Integer avanceReservationJours;
    private Integer annulationHeures;
    @Column(nullable = false)
    private Boolean tarifParPersonne = false;


    public Long getId()                             { return id; }
    public void setId(Long id)                      { this.id = id; }
    public ServiceEntity getService()               { return service; }
    public void setService(ServiceEntity service)   { this.service = service; }
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

    public ConfigService() {}

    public ConfigService(Long id, ServiceEntity service, TypeService typeService,
                         Integer dureeMinutes, Integer capaciteMinPersonnes,
                         Integer capaciteMaxPersonnes, Boolean ressourceObligatoire,
                         Boolean employeObligatoire, Boolean reservationEnGroupe,
                         Boolean fileAttenteActive, Integer avanceReservationJours,
                         Integer annulationHeures) {
        this.id = id;
        this.service = service;
        this.typeService = typeService;
        this.dureeMinutes = dureeMinutes;
        this.capaciteMinPersonnes = capaciteMinPersonnes;
        this.capaciteMaxPersonnes = capaciteMaxPersonnes;
        this.ressourceObligatoire = ressourceObligatoire;
        this.employeObligatoire = employeObligatoire;
        this.reservationEnGroupe = reservationEnGroupe;
        this.fileAttenteActive = fileAttenteActive;
        this.avanceReservationJours = avanceReservationJours;
        this.annulationHeures = annulationHeures;
        this.tarifParPersonne = false;
    }
}