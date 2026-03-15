package com.lounes.gestion_reservations.model;

public enum StatutFileAttente {
    EN_ATTENTE,
    APPELE,      // Ressource proposée — client doit accepter ou refuser
    EN_COURS,
    TERMINE,
    ANNULE,
    EXPIRE       // Client n'a pas répondu dans le délai
}