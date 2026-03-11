package com.lounes.gestion_reservations.service;

import com.lounes.gestion_reservations.dto.AvisRequest;
import com.lounes.gestion_reservations.dto.AvisResponse;
import com.lounes.gestion_reservations.model.*;
import com.lounes.gestion_reservations.repo.*;
import com.lounes.gestion_reservations.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvisService {

    @Autowired private AvisRepository avisRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private EmployeRepository employeRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private EntrepriseRepository entrepriseRepository;

    // ✅ CLIENT — laisser un avis sur une réservation terminée
    public AvisResponse create(AvisRequest request, Long userId) {
        Client client = clientRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        if (!reservation.getClient().getId().equals(client.getId()))
            throw new RuntimeException("Cette réservation ne vous appartient pas !");

        if (!reservation.getStatut().equals(StatutReservation.TERMINEE))
            throw new RuntimeException("Vous ne pouvez laisser un avis que sur une réservation terminée !");

        if (avisRepository.existsByReservation(reservation))
            throw new RuntimeException("Vous avez déjà laissé un avis pour cette réservation !");

        Avis avis = new Avis();
        avis.setClient(client);
        avis.setEmploye(reservation.getEmploye());
        avis.setService(reservation.getService());
        avis.setReservation(reservation);
        avis.setNote(request.getNote());
        avis.setCommentaire(request.getCommentaire());
        avis.setDateAvis(LocalDateTime.now());

        return toResponse(avisRepository.save(avis));
    }

    // ✅ Retourne les avis selon le rôle de l'utilisateur connecté :
    //    - SUPER_ADMIN : tous les avis
    //    - GERANT      : avis de son entreprise
    //    - EMPLOYE     : avis de l'entreprise à laquelle il appartient
    //    - CLIENT      : avis liés à ses réservations
    public List<AvisResponse> getAvisForCurrentUser() {
        UserDetailsImpl ud = (UserDetailsImpl) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        boolean isSuperAdmin = ud.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        boolean isGerant = ud.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GERANT"));
        boolean isEmploye = ud.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYE"));

        if (isSuperAdmin) {
            return avisRepository.findAll().stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }

        if (isGerant) {
            Entreprise entreprise = entrepriseRepository.findByGerantId(ud.getId())
                    .orElseThrow(() -> new RuntimeException("Entreprise du gérant introuvable"));
            return avisRepository.findByEntrepriseId(entreprise.getId()).stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }

        if (isEmploye) {
            Entreprise entreprise = entrepriseRepository.findByEmployeUserId(ud.getId())
                    .orElseThrow(() -> new RuntimeException("Entreprise de l'employé introuvable"));
            return avisRepository.findByEntrepriseId(entreprise.getId()).stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }

        // CLIENT — avis liés à ses propres réservations
        Client client = clientRepository.findByUserId(ud.getId())
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        return avisRepository.findByClientId(client.getId()).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    // ✅ Tous les utilisateurs connectés peuvent voir les avis d'un service
    public List<AvisResponse> getByService(Long serviceId) {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service non trouvé"));
        return avisRepository.findByService(service).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    private AvisResponse toResponse(Avis a) {
        String employeNom = a.getEmploye() != null ? a.getEmploye().getUser().getNom() : null;
        String employePrenom = a.getEmploye() != null ? a.getEmploye().getUser().getPrenom() : null;
        return new AvisResponse(
                a.getId(),
                a.getClient().getUser().getNom(),
                a.getClient().getUser().getPrenom(),
                employeNom,
                employePrenom,
                a.getService().getNom(),
                a.getNote(),
                a.getCommentaire(),
                a.getDateAvis()
        );
    }
}