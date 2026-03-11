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

    // ✅ CLIENT laisse un avis sur une réservation terminée
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<AvisResponse> create(
            @Valid @RequestBody AvisRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(avisService.create(request, userDetails.getId()));
    }

    // ✅ Chaque rôle voit les avis qui le concernent (filtrage dans le service)
    //    SUPER_ADMIN → tous | GERANT/EMPLOYE → leur entreprise | CLIENT → ses réservations
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT','EMPLOYE','CLIENT')")
    public ResponseEntity<List<AvisResponse>> getAll() {
        return ResponseEntity.ok(avisService.getAvisForCurrentUser());
    }

    // ✅ Tous les utilisateurs connectés peuvent voir les avis d'un service
    @GetMapping("/service/{serviceId}")
    @PreAuthorize("hasAnyRole('CLIENT','SUPER_ADMIN','GERANT','EMPLOYE')")
    public ResponseEntity<List<AvisResponse>> getByService(@PathVariable Long serviceId) {
        return ResponseEntity.ok(avisService.getByService(serviceId));
    }
}