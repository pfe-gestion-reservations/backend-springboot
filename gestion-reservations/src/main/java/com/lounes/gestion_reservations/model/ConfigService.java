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

    // Type de service — obligatoire, définit tout le comportement
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeService typeService;

    // Durée d'un créneau — obligatoire pour EMPLOYE_DEDIE, RESSOURCE_PARTAGEE, HYBRIDE
    // nullable pour FILE_ATTENTE_PURE (durée variable)
    private Integer dureeMinutes;

    // Capacité — obligatoire pour RESSOURCE_PARTAGEE (padel min=2 max=4)
    // nullable pour EMPLOYE_DEDIE et FILE_ATTENTE_PURE (toujours 1)
    private Integer capaciteMinPersonnes;
    private Integer capaciteMaxPersonnes;

    // Est-ce qu'on doit choisir une ressource (terrain, box...) ?
    // true  → RESSOURCE_PARTAGEE, HYBRIDE
    // false → EMPLOYE_DEDIE, FILE_ATTENTE_PURE
    @Column(nullable = false)
    private Boolean ressourceObligatoire = false;

    // Est-ce qu'on doit assigner un employé (secrétaire/praticien) ?
    // true  → EMPLOYE_DEDIE, HYBRIDE
    // false → RESSOURCE_PARTAGEE, FILE_ATTENTE_PURE
    @Column(nullable = false)
    private Boolean employeObligatoire = false;

    // Réservation en groupe ?
    // true  → RESSOURCE_PARTAGEE (padel = 2 à 4 joueurs)
    // false → tous les autres
    @Column(nullable = false)
    private Boolean reservationEnGroupe = false;

    // File d'attente activée pour ce service ?
    // true  → EMPLOYE_DEDIE, FILE_ATTENTE_PURE, HYBRIDE
    // false → RESSOURCE_PARTAGEE (padel, tennis)
    @Column(nullable = false)
    private Boolean fileAttenteActive = true;

    // Combien de jours à l'avance max peut-on réserver ?
    // nullable = pas de limite
    private Integer avanceReservationJours;

    // Délai minimum avant lequel on peut annuler (en heures)
    // nullable = annulation libre à tout moment
    // obligatoire pour RESSOURCE_PARTAGEE et HYBRIDE
    private Integer annulationHeures;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ServiceEntity getService() {
        return service;
    }

    public void setService(ServiceEntity service) {
        this.service = service;
    }

    public TypeService getTypeService() {
        return typeService;
    }

    public void setTypeService(TypeService typeService) {
        this.typeService = typeService;
    }

    public Integer getDureeMinutes() {
        return dureeMinutes;
    }

    public void setDureeMinutes(Integer dureeMinutes) {
        this.dureeMinutes = dureeMinutes;
    }

    public Integer getCapaciteMinPersonnes() {
        return capaciteMinPersonnes;
    }

    public void setCapaciteMinPersonnes(Integer capaciteMinPersonnes) {
        this.capaciteMinPersonnes = capaciteMinPersonnes;
    }

    public Integer getCapaciteMaxPersonnes() {
        return capaciteMaxPersonnes;
    }

    public void setCapaciteMaxPersonnes(Integer capaciteMaxPersonnes) {
        this.capaciteMaxPersonnes = capaciteMaxPersonnes;
    }

    public Boolean getRessourceObligatoire() {
        return ressourceObligatoire;
    }

    public void setRessourceObligatoire(Boolean ressourceObligatoire) {
        this.ressourceObligatoire = ressourceObligatoire;
    }

    public Boolean getEmployeObligatoire() {
        return employeObligatoire;
    }

    public void setEmployeObligatoire(Boolean employeObligatoire) {
        this.employeObligatoire = employeObligatoire;
    }

    public Boolean getReservationEnGroupe() {
        return reservationEnGroupe;
    }

    public void setReservationEnGroupe(Boolean reservationEnGroupe) {
        this.reservationEnGroupe = reservationEnGroupe;
    }

    public Boolean getFileAttenteActive() {
        return fileAttenteActive;
    }

    public void setFileAttenteActive(Boolean fileAttenteActive) {
        this.fileAttenteActive = fileAttenteActive;
    }

    public Integer getAvanceReservationJours() {
        return avanceReservationJours;
    }

    public void setAvanceReservationJours(Integer avanceReservationJours) {
        this.avanceReservationJours = avanceReservationJours;
    }

    public Integer getAnnulationHeures() {
        return annulationHeures;
    }

    public void setAnnulationHeures(Integer annulationHeures) {
        this.annulationHeures = annulationHeures;
    }

    public ConfigService() {
    }

    public ConfigService(Long id, ServiceEntity service, TypeService typeService, Integer dureeMinutes, Integer capaciteMinPersonnes, Integer capaciteMaxPersonnes, Boolean ressourceObligatoire, Boolean employeObligatoire, Boolean reservationEnGroupe, Boolean fileAttenteActive, Integer avanceReservationJours, Integer annulationHeures) {
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
    }
}