package com.lounes.gestion_reservations.service;

import com.lounes.gestion_reservations.dto.DisponibiliteRequest;
import com.lounes.gestion_reservations.dto.DisponibiliteResponse;
import com.lounes.gestion_reservations.model.*;
import com.lounes.gestion_reservations.repo.DisponibiliteRepository;
import com.lounes.gestion_reservations.repo.FileAttenteRepository;
import com.lounes.gestion_reservations.repo.ReservationRepository;
import com.lounes.gestion_reservations.repo.ServiceRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DisponibiliteService {
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private FileAttenteRepository fileAttenteRepository;
    @Autowired private DisponibiliteRepository disponibiliteRepository;
    @Autowired private ServiceRepository serviceRepository;

    private void checkChevauchement(Long serviceId, String jour, String heureDebut, String heureFin, Long excludeId) {
        LocalTime newDebut = LocalTime.parse(heureDebut);
        LocalTime newFin   = LocalTime.parse(heureFin);

        if (!newFin.isAfter(newDebut)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'heure de fin doit être après l'heure de début.");
        }

        List<Disponibilite> existantes = disponibiliteRepository.findByServiceIdAndJour(serviceId, JourSemaine.valueOf(jour));

        for (Disponibilite d : existantes) {
            if (excludeId != null && d.getId().equals(excludeId)) continue;

            LocalTime exDebut = LocalTime.parse(d.getHeureDebut().toString());
            LocalTime exFin   = LocalTime.parse(d.getHeureFin().toString());

            if (newFin.isAfter(exDebut) && newDebut.isBefore(exFin)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Ce créneau chevauche une disponibilité existante : "
                                + exDebut.toString().substring(0,5) + " → " + exFin.toString().substring(0,5));
            }
        }
    }

    public DisponibiliteResponse create(DisponibiliteRequest request) {
        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service non trouvé"));

        checkChevauchement(
                request.getServiceId(),
                request.getJour().name(),
                request.getHeureDebut().toString(),
                request.getHeureFin().toString(),
                null
        );

        Disponibilite dispo = new Disponibilite();
        dispo.setService(service);
        dispo.setJour(request.getJour());
        dispo.setHeureDebut(request.getHeureDebut());
        dispo.setHeureFin(request.getHeureFin());

        return toResponse(disponibiliteRepository.save(dispo));
    }

    public List<DisponibiliteResponse> getByService(Long serviceId) {
        if (!serviceRepository.existsById(serviceId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service non trouvé");
        return disponibiliteRepository.findByServiceId(serviceId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public DisponibiliteResponse update(Long id, DisponibiliteRequest request) {
        Disponibilite dispo = disponibiliteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disponibilité non trouvée"));

        Long serviceId = dispo.getService().getId();

        if (request.getServiceId() != null && !request.getServiceId().equals(serviceId)) {
            ServiceEntity service = serviceRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service non trouvé"));
            dispo.setService(service);
            serviceId = request.getServiceId();
        }

        checkChevauchement(
                serviceId,
                request.getJour().name(),
                request.getHeureDebut().toString(),
                request.getHeureFin().toString(),
                id
        );

        dispo.setJour(request.getJour());
        dispo.setHeureDebut(request.getHeureDebut());
        dispo.setHeureFin(request.getHeureFin());

        return toResponse(disponibiliteRepository.save(dispo));
    }

    @Transactional
    public void delete(Long id) {
        Disponibilite dispo = disponibiliteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disponibilité non trouvée"));

        LocalTime dispoDebut = dispo.getHeureDebut();
        LocalTime dispoFin   = dispo.getHeureFin();
        Long serviceId       = dispo.getService().getId();

        List<Reservation> resLiees = reservationRepository.findByServiceId(serviceId).stream()
                .filter(r -> r.getStatut() != StatutReservation.ANNULEE
                        && r.getStatut() != StatutReservation.TERMINEE)
                .filter(r -> {
                    LocalTime rDebut = r.getHeureDebut().toLocalTime();
                    return !rDebut.isBefore(dispoDebut) && rDebut.isBefore(dispoFin);
                })
                .collect(Collectors.toList());


        List<FileAttente> fileLiees = fileAttenteRepository.findByServiceId(serviceId).stream()
                .filter(f -> f.getStatut() == StatutFileAttente.EN_ATTENTE
                        || f.getStatut() == StatutFileAttente.APPELE)
                .filter(f -> {
                    if (f.getHeureDebut() == null) return false;
                    LocalTime fDebut = f.getHeureDebut().toLocalTime();
                    return !fDebut.isBefore(dispoDebut) && fDebut.isBefore(dispoFin);
                })
                .collect(Collectors.toList());

        if (!resLiees.isEmpty() || !fileLiees.isEmpty()) {
            String msg = "Ce créneau a " + resLiees.size() + " réservation(s) et "
                    + fileLiees.size() + " entrée(s) en file d'attente actives.";
            throw new ResponseStatusException(HttpStatus.CONFLICT, msg);
        }

        disponibiliteRepository.deleteById(id);
    }

    @Transactional
    public void deleteWithCancellation(Long id) {
        Disponibilite dispo = disponibiliteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disponibilité non trouvée"));

        LocalTime dispoDebut = dispo.getHeureDebut();
        LocalTime dispoFin   = dispo.getHeureFin();
        Long serviceId       = dispo.getService().getId();


        reservationRepository.findByServiceId(serviceId).stream()
                .filter(r -> r.getStatut() != StatutReservation.ANNULEE
                        && r.getStatut() != StatutReservation.TERMINEE)
                .filter(r -> {
                    LocalTime rDebut = r.getHeureDebut().toLocalTime();
                    return !rDebut.isBefore(dispoDebut) && rDebut.isBefore(dispoFin);
                })
                .forEach(r -> {
                    r.setStatut(StatutReservation.ANNULEE);
                    reservationRepository.save(r);
                });

        fileAttenteRepository.findByServiceId(serviceId).stream()
                .filter(f -> f.getStatut() == StatutFileAttente.EN_ATTENTE
                        || f.getStatut() == StatutFileAttente.APPELE)
                .filter(f -> {
                    if (f.getHeureDebut() == null) return false;
                    LocalTime fDebut = f.getHeureDebut().toLocalTime();
                    return !fDebut.isBefore(dispoDebut) && fDebut.isBefore(dispoFin);
                })
                .forEach(f -> {
                    f.setStatut(StatutFileAttente.ANNULE);
                    fileAttenteRepository.save(f);
                });

        disponibiliteRepository.deleteById(id);
    }

    private DisponibiliteResponse toResponse(Disponibilite d) {
        return new DisponibiliteResponse(
                d.getId(),
                d.getService().getId(),
                d.getService().getNom(),
                d.getJour(),
                d.getHeureDebut(),
                d.getHeureFin()
        );
    }
}