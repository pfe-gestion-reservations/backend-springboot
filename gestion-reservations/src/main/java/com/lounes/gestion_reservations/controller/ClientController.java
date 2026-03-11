package com.lounes.gestion_reservations.controller;

import com.lounes.gestion_reservations.dto.ClientRequest;
import com.lounes.gestion_reservations.dto.ClientResponse;
import com.lounes.gestion_reservations.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "*")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @GetMapping
    @PreAuthorize("hasAnyRole('GERANT','EMPLOYE')")
    public ResponseEntity<List<ClientResponse>> getAll() {
        return ResponseEntity.ok(clientService.getAll());
    }

    @GetMapping("/actifs")
    @PreAuthorize("hasAnyRole('GERANT','EMPLOYE')")
    public ResponseEntity<List<ClientResponse>> getActifs() {
        return ResponseEntity.ok(clientService.getActifs());
    }

    @GetMapping("/archives")
    @PreAuthorize("hasAnyRole('GERANT','EMPLOYE')")
    public ResponseEntity<List<ClientResponse>> getArchives() {
        return ResponseEntity.ok(clientService.getArchives());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GERANT','EMPLOYE')")
    public ResponseEntity<?> create(@RequestBody ClientRequest request) {
        return clientService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GERANT','EMPLOYE')")
    public ResponseEntity<ClientResponse> update(@PathVariable Long id,
                                                 @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.update(id, request));
    }

    @PatchMapping("/{id}/archiver")
    @PreAuthorize("hasAnyRole('GERANT','EMPLOYE')")
    public ResponseEntity<ClientResponse> archiver(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.setArchived(id, true));
    }

    @PatchMapping("/{id}/desarchiver")
    @PreAuthorize("hasAnyRole('GERANT','EMPLOYE')")
    public ResponseEntity<ClientResponse> desarchiver(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.setArchived(id, false));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERANT')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}