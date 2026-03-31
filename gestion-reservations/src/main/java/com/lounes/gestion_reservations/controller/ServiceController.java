package com.lounes.gestion_reservations.controller;

import com.lounes.gestion_reservations.dto.ServiceRequest;
import com.lounes.gestion_reservations.dto.ServiceResponse;
import com.lounes.gestion_reservations.service.ServiceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "*")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    // ✅ GET /api/services — accessible à tous les utilisateurs connectés
    @GetMapping
    @PreAuthorize("hasRole('CLIENT') or hasRole('EMPLOYE') or hasRole('GERANT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<ServiceResponse>> getAll() {
        return ResponseEntity.ok(serviceService.getAll());
    }

    // ✅ GET /api/services/by-entreprise/{entrepriseId} — SUPER_ADMIN filtre par entreprise
    @GetMapping("/by-entreprise/{entrepriseId}")
    @PreAuthorize("hasRole('GERANT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<ServiceResponse>> getByEntreprise(@PathVariable Long entrepriseId) {
        return ResponseEntity.ok(serviceService.getByEntreprise(entrepriseId));
    }

    // ✅ GET /api/services/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('EMPLOYE') or hasRole('GERANT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ServiceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceService.getById(id));
    }

    // ✅ POST /api/services — GERANT ou SUPER_ADMIN seulement
    @PostMapping
    @PreAuthorize("hasRole('GERANT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ServiceResponse> create(@Valid @RequestBody ServiceRequest request) {
        return ResponseEntity.ok(serviceService.create(request));
    }

    // ✅ PUT /api/services/{id} — GERANT ou SUPER_ADMIN seulement
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GERANT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ServiceResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody ServiceRequest request) {
        return ResponseEntity.ok(serviceService.update(id, request));
    }



    // ✅ DELETE /api/services/{id} — GERANT ou SUPER_ADMIN seulement
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERANT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        serviceService.delete(id);
        return ResponseEntity.ok("Service désactivé avec succès !");
    }
}