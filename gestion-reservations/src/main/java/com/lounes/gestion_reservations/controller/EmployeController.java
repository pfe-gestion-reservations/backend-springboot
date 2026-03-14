package com.lounes.gestion_reservations.controller;

import com.lounes.gestion_reservations.dto.EmployeRequest;
import com.lounes.gestion_reservations.dto.EmployeResponse;
import com.lounes.gestion_reservations.service.EmployeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employes")
@CrossOrigin(origins = "*")
public class EmployeController {

    @Autowired private EmployeService employeService;

    // ─── CHECK EMAIL ──────────────────────────────────────────────────────────
    @GetMapping("/check-email")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<Map<String, Object>> checkEmail(
            @RequestParam String email,
            @RequestParam(required = false) Long entrepriseId) {
        return ResponseEntity.ok(employeService.checkEmail(email, entrepriseId));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<List<EmployeResponse>> getAll() {
        return ResponseEntity.ok(employeService.getAll());
    }

    @GetMapping("/entreprise/{entrepriseId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<EmployeResponse>> getByEntreprise(@PathVariable Long entrepriseId) {
        return ResponseEntity.ok(employeService.getByEntrepriseId(entrepriseId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<EmployeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeService.getById(id));
    }

    // ─── RATTACHER un employé FREE à une entreprise ──────────────────────────
    // Accepte : { "email": "...", "entrepriseId": 5, "specialite": "..." }
    // entrepriseId est optionnel : si absent, déduit depuis le gérant connecté
    @PostMapping("/rattacher")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<?> rattacher(@RequestBody Map<String, Object> body) {
        String email      = (String) body.get("email");
        Long entrepriseId = body.get("entrepriseId") != null
                ? Long.parseLong(body.get("entrepriseId").toString()) : null;
        String specialite = (String) body.get("specialite");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Le champ email est obligatoire"));
        }
        return ResponseEntity.ok(employeService.rattacherByEmail(email, entrepriseId, specialite));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<?> create(@Valid @RequestBody EmployeRequest request) {
        return employeService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<EmployeResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody EmployeRequest request) {
        return ResponseEntity.ok(employeService.update(id, request));
    }

    @PatchMapping("/{id}/archiver")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<?> archiver(@PathVariable Long id) {
        employeService.archiver(id);
        return ResponseEntity.ok("Employé archivé avec succès !");
    }

    @PatchMapping("/{id}/desarchiver")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<?> desarchiver(@PathVariable Long id) {
        employeService.desarchiver(id);
        return ResponseEntity.ok("Employé désarchivé avec succès !");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<?> desactiver(@PathVariable Long id) {
        employeService.desactiver(id);
        return ResponseEntity.ok("Employé désactivé avec succès !");
    }

    @PutMapping("/{id}/reactiver")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GERANT')")
    public ResponseEntity<?> reactiver(@PathVariable Long id) {
        employeService.reactiver(id);
        return ResponseEntity.ok("Employé réactivé avec succès !");
    }
}