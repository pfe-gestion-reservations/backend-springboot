package com.lounes.gestion_reservations.controller;

import com.lounes.gestion_reservations.dto.*;
import com.lounes.gestion_reservations.service.GerantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequestMapping("/api/gerants")
@CrossOrigin(origins = "*")
public class GerantController {

    @Autowired private GerantService gerantService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<GerantResponse>> getAll() {
        return ResponseEntity.ok(gerantService.getAll());
    }

    @GetMapping("/disponibles")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<GerantResponse>> getDisponibles() {
        return ResponseEntity.ok(gerantService.getDisponibles());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<GerantResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(gerantService.getById(id));
    }

    // Vérifier email avant création gérant
    @GetMapping("/check-email")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<EmployeCheckResponse> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(gerantService.checkEmail(email));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody GerantRequest request) {
        try {
            return ResponseEntity.ok(gerantService.update(id, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Archiver un gérant — remplaçantId accepté en query param OU dans le body
    @PatchMapping("/{id}/archiver")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> archiver(@PathVariable Long id,
                                      @RequestParam(required = false) Long remplacantId,
                                      @RequestBody(required = false) java.util.Map<String, Long> body) {
        Long rId = remplacantId != null ? remplacantId
                : (body != null ? body.get("remplacantId") : null);
        return ResponseEntity.ok(gerantService.archiver(id, rId));
    }

    @PatchMapping("/{id}/desarchiver")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<GerantResponse> desarchiver(@PathVariable Long id) {
        return ResponseEntity.ok(gerantService.desarchiver(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        gerantService.delete(id);
        return ResponseEntity.ok("Gérant supprimé avec succès !");
    }
}