package com.lounes.gestion_reservations.controller;

import com.lounes.gestion_reservations.dto.ConfigServiceRequest;
import com.lounes.gestion_reservations.dto.ConfigServiceResponse;
import com.lounes.gestion_reservations.service.ConfigServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config-services")
public class ConfigServiceController {

    @Autowired private ConfigServiceService configServiceService;

    // Récupérer la config d'un service
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<ConfigServiceResponse> getByService(@PathVariable Long serviceId) {
        return ResponseEntity.ok(configServiceService.getByService(serviceId));
    }

    // Créer ou mettre à jour la config d'un service (upsert)
    @PostMapping
    @PreAuthorize("hasAnyRole('GERANT','SUPER_ADMIN')")
    public ResponseEntity<ConfigServiceResponse> save(@RequestBody ConfigServiceRequest request) {
        return ResponseEntity.ok(configServiceService.save(request));
    }

    // Supprimer la config d'un service
    @DeleteMapping("/service/{serviceId}")
    @PreAuthorize("hasAnyRole('GERANT','SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long serviceId) {
        configServiceService.delete(serviceId);
        return ResponseEntity.noContent().build();
    }
}