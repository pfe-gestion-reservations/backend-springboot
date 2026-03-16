package com.lounes.gestion_reservations.controller;

import com.lounes.gestion_reservations.dto.ClientRequest;
import com.lounes.gestion_reservations.dto.ClientResponse;
import com.lounes.gestion_reservations.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "*")
public class ClientController {

    @Autowired
    private ClientService clientService;

    // ─── LOOKUP PAR TÉLÉPHONE ─────────────────────────────────────────────────
    @GetMapping("/by-telephone/{numtel}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT','EMPLOYE')")
    public ResponseEntity<Map<String, Object>> findByTelephone(
            @PathVariable String numtel) {
        return ResponseEntity.ok(clientService.findByTelephone(numtel));
    }

    // ─── CHECK EMAIL (par @RequestParam pour éviter pb encodage avec @) ───────
    @GetMapping("/check-email")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT','EMPLOYE')")
    public ResponseEntity<?> checkEmail(
            @RequestParam String email) {
        return ResponseEntity.ok(clientService.checkEmail(email));
    }

    // ─── ASSOCIER UN CLIENT EXISTANT À UNE ENTREPRISE ────────────────────────
    @PostMapping("/{clientId}/entreprise/{entrepriseId}/associer")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GERANT')")
    public ResponseEntity<?> associerAEntreprise(
            @PathVariable Long clientId,
            @PathVariable Long entrepriseId) {
        return clientService.associerAEntreprise(clientId, entrepriseId);
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT','EMPLOYE')")
    public ResponseEntity<List<ClientResponse>> getAll() {
        return ResponseEntity.ok(clientService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT','EMPLOYE')")
    public ResponseEntity<?> create(@RequestBody ClientRequest request) {
        return clientService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT','EMPLOYE')")
    public ResponseEntity<ClientResponse> update(@PathVariable Long id,
                                                 @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.update(id, request));
    }

    @PatchMapping("/{id}/archiver")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT','EMPLOYE')")
    public ResponseEntity<ClientResponse> archiver(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.setArchived(id, true));
    }

    @PatchMapping("/{id}/desarchiver")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT','EMPLOYE')")
    public ResponseEntity<ClientResponse> desarchiver(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.setArchived(id, false));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/desarchiver-associer")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT','EMPLOYE')")
    public ResponseEntity<?> desarchiverEtAssocier(@PathVariable Long id,
                                                   @RequestParam(required = false) Long entrepriseId) {
        return clientService.desarchiverEtAssocier(id, entrepriseId);
    }

    @DeleteMapping("/{clientId}/entreprise/{entrepriseId}/dissocier")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GERANT','EMPLOYE')")
    public ResponseEntity<?> dissocierDeEntreprise(
            @PathVariable Long clientId,
            @PathVariable Long entrepriseId) {
        return clientService.dissocierDeEntreprise(clientId, entrepriseId);
    }
}