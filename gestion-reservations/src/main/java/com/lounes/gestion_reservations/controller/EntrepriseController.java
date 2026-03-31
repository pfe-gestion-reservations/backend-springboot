package com.lounes.gestion_reservations.controller;

import com.lounes.gestion_reservations.dto.EntrepriseRequest;
import com.lounes.gestion_reservations.dto.EntrepriseResponse;
import com.lounes.gestion_reservations.service.EntrepriseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entreprises")
@CrossOrigin(origins = "*")
public class EntrepriseController {

    @Autowired private EntrepriseService entrepriseService;
    //recuperer toutes les entrep
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<EntrepriseResponse>> getAll() {
        return ResponseEntity.ok(entrepriseService.getAll());
    }
    //recuperer une entrep bien precise
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<EntrepriseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(entrepriseService.getById(id));
    }
    //creer une entrep
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<EntrepriseResponse> create(@Valid @RequestBody EntrepriseRequest request) {
        return ResponseEntity.ok(entrepriseService.create(request));
    }
    //modifier une entrep
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<EntrepriseResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody EntrepriseRequest request) {
        return ResponseEntity.ok(entrepriseService.update(id, request));
    }

    //afficher les entrep par secteur
    @GetMapping("/by-secteur/{secteurId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<EntrepriseResponse>> getBySecteur(@PathVariable Long secteurId) {
        return ResponseEntity.ok(entrepriseService.getBySecteur(secteurId));
    }
    //supprimer une entrep
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        entrepriseService.delete(id);
        return ResponseEntity.ok("Entreprise supprimée !");
    }
}