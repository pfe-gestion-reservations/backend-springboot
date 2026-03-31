package com.lounes.gestion_reservations.controller;

import com.lounes.gestion_reservations.dto.AvisRequest;
import com.lounes.gestion_reservations.dto.AvisResponse;
import com.lounes.gestion_reservations.security.UserDetailsImpl;
import com.lounes.gestion_reservations.service.AvisService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/avis")
@CrossOrigin(origins = "*")
public class AvisController {

    @Autowired private AvisService avisService;
    //le client laisse son avis sur une reseravtion terminé
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<AvisResponse> create(
            @Valid @RequestBody AvisRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(avisService.create(request, userDetails.getId()));
    }

    //chaque user vois ses propres avis
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT','EMPLOYE','CLIENT')")
    public ResponseEntity<List<AvisResponse>> getAll() {
        return ResponseEntity.ok(avisService.getAvisForCurrentUser());
    }

    //recuperer tous les avis d un service donné
    //a verifier apres
    @GetMapping("/service/{serviceId}")
    @PreAuthorize("hasAnyRole('CLIENT','SUPER_ADMIN','GERANT','EMPLOYE')")
    public ResponseEntity<List<AvisResponse>> getByService(@PathVariable Long serviceId) {
        return ResponseEntity.ok(avisService.getByService(serviceId));
    }
}