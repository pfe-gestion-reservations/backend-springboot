package com.lounes.gestion_reservations.controller;

import com.lounes.gestion_reservations.dto.RessourceRequest;
import com.lounes.gestion_reservations.dto.RessourceResponse;
import com.lounes.gestion_reservations.service.RessourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ressources")
public class RessourceController {

    @Autowired private RessourceService ressourceService;

    //toutes les ressources
    @GetMapping
    @PreAuthorize("hasAnyRole('GERANT','EMPLOYE','SUPER_ADMIN')")
    public List<RessourceResponse> getAll() {
        return ressourceService.getAll();
    }

    //ressources d un service specifique
    @GetMapping("/service/{serviceId}")
    public List<RessourceResponse> getByService(@PathVariable Long serviceId) {
        return ressourceService.getByService(serviceId);
    }
    //creer une ressource
    @PostMapping
    @PreAuthorize("hasAnyRole('GERANT','SUPER_ADMIN')")
    public ResponseEntity<RessourceResponse> create(@RequestBody RessourceRequest request) {
        return ResponseEntity.ok(ressourceService.create(request));
    }
    //maj une ressource
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GERANT','SUPER_ADMIN')")
    public ResponseEntity<RessourceResponse> update(
            @PathVariable Long id,
            @RequestBody RessourceRequest request) {
        return ResponseEntity.ok(ressourceService.update(id, request));
    }
    //archiver une ressource
    @PatchMapping("/{id}/archiver")
    @PreAuthorize("hasAnyRole('GERANT','SUPER_ADMIN')")
    public ResponseEntity<RessourceResponse> archiver(@PathVariable Long id) {
        return ResponseEntity.ok(ressourceService.archiver(id));
    }
    //desarchiver une ressource
    @PatchMapping("/{id}/desarchiver")
    @PreAuthorize("hasAnyRole('GERANT','SUPER_ADMIN')")
    public ResponseEntity<RessourceResponse> desarchiver(@PathVariable Long id) {
        return ResponseEntity.ok(ressourceService.desarchiver(id));
    }
}