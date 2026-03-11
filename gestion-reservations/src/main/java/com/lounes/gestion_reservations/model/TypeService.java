package com.lounes.gestion_reservations.model;

public enum TypeService {
    EMPLOYE_DEDIE,       // Coiffure, Médecin — 1 client + 1 employé/ressource humaine
    RESSOURCE_PARTAGEE,  // Padel, Tennis — créneau sur un terrain/espace
    FILE_ATTENTE_PURE,   // Pharmacie, Admin — walk-in sans réservation
    HYBRIDE              // Garage — file d'attente + créneau possible
}