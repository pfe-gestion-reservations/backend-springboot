package com.lounes.gestion_reservations.controller;

import com.lounes.gestion_reservations.dto.DisponibiliteRequest;
import com.lounes.gestion_reservations.dto.DisponibiliteResponse;
import com.lounes.gestion_reservations.service.DisponibiliteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disponibilites")
@CrossOrigin(origins = "*")

public class DisponibiliteController {

    @Autowired
    private DisponibiliteService disponibiliteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT')")
    public ResponseEntity<DisponibiliteResponse> create(
            @Valid @RequestBody DisponibiliteRequest request) {
        return ResponseEntity.ok(disponibiliteService.create(request));
    }

    // ← /employe/{id} remplacé par /service/{serviceId}
    @GetMapping("/service/{serviceId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT','EMPLOYE','CLIENT')")
    public ResponseEntity<List<DisponibiliteResponse>> getByService(
            @PathVariable Long serviceId) {
        return ResponseEntity.ok(disponibiliteService.getByService(serviceId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT')")
    public ResponseEntity<DisponibiliteResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DisponibiliteRequest request) {
        return ResponseEntity.ok(disponibiliteService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        disponibiliteService.delete(id);
        return ResponseEntity.ok("Disponibilité supprimée !");
    }

    @DeleteMapping("/{id}/force")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT')")
    public ResponseEntity<Void> deleteForce(@PathVariable Long id) {
        disponibiliteService.deleteWithCancellation(id);
        return ResponseEntity.noContent().build();
    }
}