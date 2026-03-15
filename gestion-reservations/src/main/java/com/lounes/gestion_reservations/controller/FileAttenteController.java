package com.lounes.gestion_reservations.controller;

import com.lounes.gestion_reservations.dto.FileAttenteRequest;
import com.lounes.gestion_reservations.dto.FileAttenteResponse;
import com.lounes.gestion_reservations.security.UserDetailsImpl;
import com.lounes.gestion_reservations.service.FileAttenteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/file-attente")
@CrossOrigin(origins = "*")
public class FileAttenteController {

    @Autowired private FileAttenteService fileAttenteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT','EMPLOYE','GERANT','SUPER_ADMIN')")
    public ResponseEntity<FileAttenteResponse> ajouter(
            @Valid @RequestBody FileAttenteRequest request) {
        return ResponseEntity.ok(fileAttenteService.ajouter(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT','EMPLOYE')")
    public ResponseEntity<List<FileAttenteResponse>> getAll() {
        return ResponseEntity.ok(fileAttenteService.getAll());
    }

    // File d'attente par service + créneau (RESSOURCE_PARTAGEE)
    @GetMapping("/by-service/{serviceId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT','EMPLOYE')")
    public ResponseEntity<List<FileAttenteResponse>> getByServiceEtCreneau(
            @PathVariable Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime heureDebut) {
        return ResponseEntity.ok(fileAttenteService.getByServiceEtCreneau(serviceId, heureDebut));
    }

    // Client accepte la proposition
    @PutMapping("/{id}/accepter")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<FileAttenteResponse> accepter(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(fileAttenteService.accepter(id, userDetails.getId()));
    }

    // Client refuse la proposition → passe au suivant
    @PutMapping("/{id}/refuser")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<FileAttenteResponse> refuser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(fileAttenteService.refuser(id, userDetails.getId()));
    }

    @PutMapping("/{id}/appeler")
    @PreAuthorize("hasAnyRole('EMPLOYE','GERANT','SUPER_ADMIN')")
    public ResponseEntity<FileAttenteResponse> appeler(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(fileAttenteService.appeler(id, userDetails.getId()));
    }

    @PutMapping("/{id}/demarrer")
    @PreAuthorize("hasAnyRole('EMPLOYE','GERANT','SUPER_ADMIN')")
    public ResponseEntity<FileAttenteResponse> demarrer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(fileAttenteService.demarrer(id, userDetails.getId()));
    }

    @PutMapping("/{id}/terminer")
    @PreAuthorize("hasAnyRole('EMPLOYE','GERANT','SUPER_ADMIN')")
    public ResponseEntity<FileAttenteResponse> terminer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(fileAttenteService.terminer(id, userDetails.getId()));
    }

    @PutMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('EMPLOYE','GERANT','SUPER_ADMIN','CLIENT')")
    public ResponseEntity<FileAttenteResponse> annuler(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(fileAttenteService.annuler(id, userDetails.getId()));
    }

    @PutMapping("/{id}/annuler/admin")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT')")
    public ResponseEntity<FileAttenteResponse> annulerAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(fileAttenteService.annulerAdmin(id));
    }
}