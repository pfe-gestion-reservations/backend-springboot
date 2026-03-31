package com.lounes.gestion_reservations.service;

import com.lounes.gestion_reservations.dto.CreneauResponse;
import com.lounes.gestion_reservations.model.*;
import com.lounes.gestion_reservations.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CreneauService {

    @Autowired private DisponibiliteRepository disponibiliteRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private ServiceRepository serviceRepository;

    public List<CreneauResponse> getCreneauxDisponibles(Long serviceId, LocalDate date) {

        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service non trouvé"));

        JourSemaine jour = convertirJour(date.getDayOfWeek());

        List<Disponibilite> dispos = disponibiliteRepository.findByServiceIdAndJour(serviceId, jour);
        if (dispos.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ce service n'est pas disponible ce jour !");


        int duree = service.getConfig() != null && service.getConfig().getDureeMinutes() != null
                ? service.getConfig().getDureeMinutes()
                : service.getDureeMinutes();


        LocalDateTime debutJour = date.atStartOfDay();
        LocalDateTime finJour   = date.atTime(23, 59, 59);
        List<Reservation> reservations = reservationRepository
                .findByServiceIdAndHeureDebutBetween(serviceId, debutJour, finJour); // ← corrigé

        List<CreneauResponse> creneaux = new ArrayList<>();

        for (Disponibilite dispo : dispos) {
            LocalTime cursor   = dispo.getHeureDebut();
            LocalTime finDispo = dispo.getHeureFin();

            while (!cursor.plusMinutes(duree).isAfter(finDispo)) {
                LocalTime creneauFin  = cursor.plusMinutes(duree);
                LocalTime finalCursor = cursor;

                boolean occupe = reservations.stream().anyMatch(r -> {
                    LocalTime rDebut = r.getHeureDebut().toLocalTime(); // ← corrigé
                    LocalTime rFin   = r.getHeureFin().toLocalTime();   // ← corrigé
                    return finalCursor.isBefore(rFin) && creneauFin.isAfter(rDebut);
                });

                if (!occupe)
                    creneaux.add(new CreneauResponse(cursor, creneauFin));

                cursor = cursor.plusMinutes(duree);
            }
        }

        return creneaux;
    }

    private JourSemaine convertirJour(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:    return JourSemaine.LUNDI;
            case TUESDAY:   return JourSemaine.MARDI;
            case WEDNESDAY: return JourSemaine.MERCREDI;
            case THURSDAY:  return JourSemaine.JEUDI;
            case FRIDAY:    return JourSemaine.VENDREDI;
            case SATURDAY:  return JourSemaine.SAMEDI;
            case SUNDAY:    return JourSemaine.DIMANCHE;
            default: throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Jour invalide !");
        }
    }
}