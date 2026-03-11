package com.lounes.gestion_reservations.controller;

import com.lounes.gestion_reservations.dto.*;
import com.lounes.gestion_reservations.model.ERole;
import com.lounes.gestion_reservations.model.User;
import com.lounes.gestion_reservations.repo.EntrepriseRepository;
import com.lounes.gestion_reservations.repo.UserRepository;
import com.lounes.gestion_reservations.service.EmployeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employes")
@CrossOrigin(origins = "*")
public class EmployeController {

    @Autowired private EmployeService employeService;
    @Autowired private UserRepository userRepository;
    @Autowired private EntrepriseRepository entrepriseRepository;

    // ── Utilitaire : résoudre l'entreprise selon le rôle appelant ────────────
    private Long resolveEntrepriseId(Long requestedId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName() == ERole.ROLE_SUPER_ADMIN);

        if (isSuperAdmin) {
            if (requestedId == null)
                throw new RuntimeException("entrepriseId obligatoire pour SUPER_ADMIN");
            return requestedId;
        }
        // GERANT → déduit de son entreprise
        return entrepriseRepository.findByGerantId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Aucune entreprise trouvée pour ce gérant"))
                .getId();
    }

    // ── Vérifier un email avant création ─────────────────────────────────────
    @GetMapping("/check-email")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<EmployeCheckResponse> checkEmail(
            @RequestParam String email,
            @RequestParam(required = false) Long entrepriseId) {
        Long eid = resolveEntrepriseId(entrepriseId);
        return ResponseEntity.ok(employeService.checkEmail(email, eid));
    }

    // ── Créer nouveau compte employé ─────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<EmployeResponse> create(@Valid @RequestBody EmployeRequest request) {
        Long eid = resolveEntrepriseId(request.getEntrepriseId());
        if (request.getPassword() == null || request.getPassword().isBlank())
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(employeService.create(request, eid));
    }

    // ── Rattacher un user existant libre ─────────────────────────────────────
    @PostMapping("/rattacher")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<EmployeResponse> rattacher(@RequestBody RattachementRequest req) {
        Long eid = resolveEntrepriseId(req.getEntrepriseId());
        return ResponseEntity.ok(employeService.rattacher(req.getUserId(), eid, req.getSpecialite()));
    }

    // ── Liste ─────────────────────────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<List<EmployeResponse>> getAll() {
        return ResponseEntity.ok(employeService.getAll());
    }

    @GetMapping("/entreprise/{entrepriseId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<List<EmployeResponse>> getByEntreprise(@PathVariable Long entrepriseId) {
        return ResponseEntity.ok(employeService.getByEntrepriseId(entrepriseId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<EmployeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeService.getById(id));
    }

    // ── Modifier ──────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<EmployeResponse> update(@PathVariable Long id,
                                                  @RequestBody EmployeRequest request) {
        return ResponseEntity.ok(employeService.update(id, request));
    }

    // ── Archiver / désarchiver ────────────────────────────────────────────────
    @PatchMapping("/{id}/archiver")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<EmployeResponse> archiver(@PathVariable Long id) {
        return ResponseEntity.ok(employeService.archiver(id));
    }

    @PatchMapping("/{id}/desarchiver")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<EmployeResponse> desarchiver(@PathVariable Long id) {
        return ResponseEntity.ok(employeService.desarchiver(id));
    }
}