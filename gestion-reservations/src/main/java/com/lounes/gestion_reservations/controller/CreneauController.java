package com.lounes.gestion_reservations.controller;

import com.lounes.gestion_reservations.dto.CreneauResponse;
import com.lounes.gestion_reservations.service.CreneauService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/creneaux")
@CrossOrigin(origins = "*")
public class CreneauController {

    @Autowired
    private CreneauService creneauService;
    //recuperer les creneaux dispo pour un service a une date donnée
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT','EMPLOYE','CLIENT')")
    public ResponseEntity<List<CreneauResponse>> getCreneaux(
            @RequestParam Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(creneauService.getCreneauxDisponibles(serviceId, date));
    }
}