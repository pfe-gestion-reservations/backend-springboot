package com.lounes.gestion_reservations.controller;

import com.lounes.gestion_reservations.dto.CreateReservationRequest;
import com.lounes.gestion_reservations.dto.ReservationResponse;
import com.lounes.gestion_reservations.model.StatutReservation;
import com.lounes.gestion_reservations.service.ReservationService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")

public class ReservationController {

    @Autowired
    private ReservationService reservationService;
    //afficher toutes les reservations
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT','EMPLOYE','CLIENT')")
    public ResponseEntity<List<ReservationResponse>> getAll() {
        //le filtrage par role est géré par reservation service
        return ResponseEntity.ok(reservationService.getAll());
    }
    //creer une reservation
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT','EMPLOYE','CLIENT')")
    public ResponseEntity<ReservationResponse> create(
            @Valid @RequestBody CreateReservationRequest request) {
        return ResponseEntity.ok(reservationService.create(request));
    }
    //maj reservztion
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT','EMPLOYE')")
    public ResponseEntity<ReservationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateReservationRequest request) {
        return ResponseEntity.ok(reservationService.update(id, request));
    }
    //maj statut
    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT','EMPLOYE')")
    public ResponseEntity<ReservationResponse> updateStatut(
            @PathVariable Long id,
            @RequestParam String statut) {
        return ResponseEntity.ok(reservationService.updateStatut(id, statut));
    }
    //annuler par id client
    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT','EMPLOYE','CLIENT')")
    public ResponseEntity<ReservationResponse> annulerParClient(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.annulerParClient(id));
    }
    //supprimer reservation
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}