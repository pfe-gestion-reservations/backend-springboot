package com.lounes.gestion_reservations.dto;

import java.time.LocalTime;

public class CreneauResponse {
    private LocalTime heureDebut;
    private LocalTime heureFin;

    public CreneauResponse(LocalTime heureDebut, LocalTime heureFin) {
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

    public LocalTime getHeureDebut() { return heureDebut; }
    public LocalTime getHeureFin() { return heureFin; }
}