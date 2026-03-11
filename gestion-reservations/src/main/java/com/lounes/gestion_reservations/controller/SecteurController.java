package com.lounes.gestion_reservations.controller;

import com.lounes.gestion_reservations.dto.SecteurRequest;
import com.lounes.gestion_reservations.dto.SecteurResponse;
import com.lounes.gestion_reservations.service.SecteurService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/secteurs")
@CrossOrigin(origins = "*")
public class SecteurController {

    @Autowired private SecteurService secteurService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<SecteurResponse>> getAll() {
        return ResponseEntity.ok(secteurService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<SecteurResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(secteurService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<SecteurResponse> create(@Valid @RequestBody SecteurRequest request) {
        return ResponseEntity.ok(secteurService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<SecteurResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody SecteurRequest request) {
        return ResponseEntity.ok(secteurService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        secteurService.delete(id);
        return ResponseEntity.ok("Secteur supprimé !");
    }
}