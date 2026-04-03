package com.lounes.gestion_reservations.controller;

import com.lounes.gestion_reservations.dto.EmployeRequest;
import com.lounes.gestion_reservations.dto.EmployeResponse;
import com.lounes.gestion_reservations.model.Employe;
import com.lounes.gestion_reservations.service.EmployeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employes")
@CrossOrigin(origins = "*")
public class EmployeController {

    @Autowired private EmployeService employeService;

    //verifier existanec mail
    @GetMapping("/check-email")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<Map<String, Object>> checkEmail(
            @RequestParam String email,
            @RequestParam(required = false) Long entrepriseId) {
        return ResponseEntity.ok(employeService.checkEmail(email, entrepriseId));
    }

    //recuperer tous les employes
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<List<EmployeResponse>> getAll() {
        return ResponseEntity.ok(employeService.getAll());
    }
    //recuperer les employes d une entreprise precise
    @GetMapping("/entreprise/{entrepriseId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<List<EmployeResponse>> getByEntreprise(@PathVariable Long entrepriseId) {
        return ResponseEntity.ok(employeService.getByEntrepriseId(entrepriseId));
    }
    // recuperer un employe precis
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<EmployeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeService.getById(id));
    }

    //rattacher un employe libre a une entreprise
    @PostMapping("/rattacher")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<?> rattacher(@RequestBody Map<String, Object> body) {
        String email      = (String) body.get("email");
        Long entrepriseId = body.get("entrepriseId") != null
                ? Long.parseLong(body.get("entrepriseId").toString()) : null;

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Le champ email est obligatoire"));
        }
        return ResponseEntity.ok(employeService.rattacherByEmail(email, entrepriseId));
    }
    //creer employe
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<?> create(@Valid @RequestBody EmployeRequest request) {
        return employeService.create(request);
    }
    //maj employe
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<EmployeResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody EmployeRequest request) {
        return ResponseEntity.ok(employeService.update(id, request));
    }
    //archiver employe
    @PatchMapping("/{id}/archiver")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<?> archiver(@PathVariable Long id) {
        employeService.archiver(id);
        return ResponseEntity.ok("Employé archivé avec succès !");
    }
    //desarchiver employe
    @PatchMapping("/{id}/desarchiver")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<?> desarchiver(@PathVariable Long id) {
        employeService.desarchiver(id);
        return ResponseEntity.ok("Employé désarchivé avec succès !");
    }

    //desarchiver+ rattchaer employe
    @PatchMapping("/{id}/desarchiver-rattacher")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<?> desarchiverEtRattacher(@PathVariable Long id) {
        employeService.desarchiverEtRattacher(id);
        return ResponseEntity.ok("Employé désarchivé et rattaché avec succès !");
    }
    //supprimer
    @DeleteMapping("/{id}/supprimer")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> supprimerDefinitivement(@PathVariable Long id) {
        employeService.supprimerDefinitivement(id);
        return ResponseEntity.ok("Employé supprimé définitivement.");
    }

    /*@DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<?> desactiver(@PathVariable Long id) {
        employeService.desactiver(id);
        return ResponseEntity.ok("Employé désactivé avec succès !");
    }*/
/*
    @PutMapping("/{id}/reactiver")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<?> reactiver(@PathVariable Long id) {
        employeService.reactiver(id);
        return ResponseEntity.ok("Employé réactivé avec succès !");
    }
*/

}