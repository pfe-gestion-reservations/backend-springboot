package com.lounes.gestion_reservations.controller;

import com.lounes.gestion_reservations.dto.FileAttenteRequest;
import com.lounes.gestion_reservations.dto.FileAttenteResponse;
import com.lounes.gestion_reservations.security.UserDetailsImpl;
import com.lounes.gestion_reservations.service.FileAttenteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/file-attente")
@CrossOrigin(origins = "*")
public class FileAttenteController {

    @Autowired private FileAttenteService fileAttenteService;

    // ✅ Seul l'EMPLOYE (secrétaire) peut ajouter un client en file
    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYE','SUPER_ADMIN')")
    public ResponseEntity<FileAttenteResponse> ajouter(
            @Valid @RequestBody FileAttenteRequest request) {
        return ResponseEntity.ok(fileAttenteService.ajouter(request));
    }

    // ✅ EMPLOYE/GERANT/SUPER_ADMIN voient la file de leur entreprise (filtrage dans le service)
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT','EMPLOYE')")
    public ResponseEntity<List<FileAttenteResponse>> getAll() {
        return ResponseEntity.ok(fileAttenteService.getAll());
    }

    @PutMapping("/{id}/appeler")
    @PreAuthorize("hasAnyRole('EMPLOYE','SUPER_ADMIN')")
    public ResponseEntity<FileAttenteResponse> appeler(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(fileAttenteService.appeler(id, userDetails.getId()));
    }

    @PutMapping("/{id}/demarrer")
    @PreAuthorize("hasAnyRole('EMPLOYE','SUPER_ADMIN')")
    public ResponseEntity<FileAttenteResponse> demarrer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(fileAttenteService.demarrer(id, userDetails.getId()));
    }

    @PutMapping("/{id}/terminer")
    @PreAuthorize("hasAnyRole('EMPLOYE','SUPER_ADMIN')")
    public ResponseEntity<FileAttenteResponse> terminer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(fileAttenteService.terminer(id, userDetails.getId()));
    }

    @PutMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('EMPLOYE','SUPER_ADMIN')")
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