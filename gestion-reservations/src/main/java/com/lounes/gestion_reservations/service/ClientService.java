package com.lounes.gestion_reservations.service;

import com.lounes.gestion_reservations.dto.ClientRequest;
import com.lounes.gestion_reservations.dto.ClientResponse;
import com.lounes.gestion_reservations.model.*;
import com.lounes.gestion_reservations.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClientService {

    @Autowired private ClientRepository clientRepository;
    @Autowired private EmployeRepository employeRepository;
    @Autowired private EntrepriseRepository entrepriseRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private FileAttenteRepository fileAttenteRepository;
    @Autowired private AvisRepository avisRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non trouvé"));
    }

    private boolean isGerant(User user) {
        return user.getRoles().stream().anyMatch(r -> r.getName() == ERole.ROLE_GERANT);
    }

    private boolean isSuperAdmin(User user) {
        return user.getRoles().stream().anyMatch(r -> r.getName() == ERole.ROLE_SUPER_ADMIN);
    }

    private boolean isGerantOrSuperAdmin(User user) {
        return isGerant(user) || isSuperAdmin(user);
    }

    private Employe getEmployeFromUser(User user) {
        return employeRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé non trouvé"));
    }

    private void checkOwnership(Client client, User currentUser) {
        if (isGerantOrSuperAdmin(currentUser)) return;
        Employe employe = getEmployeFromUser(currentUser);
        if (client.getCreatedBy() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé !");
        if (!client.getCreatedBy().equals(employe.getUser().getEmail()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé !");
    }

    private Entreprise resolveEntreprise(User currentUser, Long entrepriseId) {
        if (isSuperAdmin(currentUser)) {
            if (entrepriseId == null) return null;
            return entrepriseRepository.findById(entrepriseId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entreprise non trouvée"));
        } else if (isGerant(currentUser)) {
            return entrepriseRepository.findByGerantId(currentUser.getId()).orElse(null);
        } else {
            Employe emp = employeRepository.findByUser(currentUser).orElse(null);
            return emp != null ? emp.getEntreprise() : null;
        }
    }

    private ClientResponse toResponse(Client client) {
        ClientResponse resp = new ClientResponse(
                client.getId(),
                client.getUser().getNom(),
                client.getUser().getPrenom(),
                client.getUser().getEmail(),
                client.getNumtel(),
                client.getArchived(),
                client.getCreatedBy()
        );
        List<ClientResponse.EntrepriseInfo> entrepriseInfos = client.getEntreprises().stream()
                .map(e -> new ClientResponse.EntrepriseInfo(
                        e.getId(), e.getNom(),
                        e.getSecteur() != null ? e.getSecteur().getNom() : null
                ))
                .collect(Collectors.toList());
        resp.setEntreprises(entrepriseInfos);
        return resp;
    }

    // ─── CHECK TÉLÉPHONE ──────────────────────────────────────────────────────
    public Map<String, Object> findByTelephone(String numtel) {
        Optional<Client> clientOpt = clientRepository.findByNumtel(numtel);
        if (clientOpt.isEmpty()) return Map.of("status", "NOT_FOUND");
        Client client = clientOpt.get();
        if (Boolean.TRUE.equals(client.getArchived())) {
            return Map.of("status", "ARCHIVED",
                    "clientId", client.getId(),
                    "nom", client.getUser().getNom(),
                    "prenom", client.getUser().getPrenom(),
                    "email", client.getUser().getEmail(),
                    "numtel", numtel);
        }
        return Map.of("status", "ALREADY_TAKEN",
                "clientId", client.getId(),
                "nom", client.getUser().getNom(),
                "prenom", client.getUser().getPrenom(),
                "email", client.getUser().getEmail(),
                "numtel", numtel);
    }

    // ─── CHECK EMAIL ──────────────────────────────────────────────────────────
    public Map<String, Object> checkEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return Map.of("status", "NOT_FOUND");
        User user = userOpt.get();
        boolean isClient = user.getRoles().stream().anyMatch(r -> r.getName() == ERole.ROLE_CLIENT);
        if (isClient) {
            Optional<Client> clientOpt = clientRepository.findByUser(user);
            if (clientOpt.isPresent() && Boolean.TRUE.equals(clientOpt.get().getArchived())) {
                Client client = clientOpt.get();
                return Map.of("status", "ROLE_CLIENT_ARCHIVED",
                        "clientId", client.getId(),
                        "nom", user.getNom(),
                        "prenom", user.getPrenom(),
                        "email", user.getEmail());
            }
            Client client = clientOpt.orElseThrow();
            return Map.of("status", "ROLE_CLIENT",
                    "clientId", client.getId(),
                    "nom", user.getNom(),
                    "prenom", user.getPrenom(),
                    "email", user.getEmail());
        } else {
            return Map.of("status", "EMAIL_OTHER_ROLE",
                    "message", "Un utilisateur avec un autre rôle possède déjà ce mail. Choisissez un autre mail.");
        }
    }

    // ─── ASSOCIER UN CLIENT EXISTANT À UNE ENTREPRISE ────────────────────────
    public ResponseEntity<?> associerAEntreprise(Long clientId, Long entrepriseId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé"));
        Entreprise entreprise = entrepriseRepository.findById(entrepriseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entreprise non trouvée"));
        boolean dejaAssocie = client.getEntreprises().stream().anyMatch(e -> e.getId().equals(entrepriseId));
        if (dejaAssocie) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "ALREADY_ASSOCIATED",
                    "message", "Ce client est déjà associé à cette entreprise."));
        }
        if (Boolean.TRUE.equals(client.getUser().getArchived())) {
            client.getUser().setArchived(false);
            client.setArchived(false);
            userRepository.save(client.getUser());
        }
        client.addEntreprise(entreprise);
        client.setCreatedBy(getCurrentUser().getEmail());
        Client saved = clientRepository.save(client);
        return ResponseEntity.ok(Map.of(
                "status", "ASSOCIATED",
                "message", "Client associé à " + entreprise.getNom() + " avec succès.",
                "client", toResponse(saved)));
    }

    // ─── DÉSARCHIVER ET ASSOCIER ──────────────────────────────────────────────
    public ResponseEntity<?> desarchiverEtAssocier(Long clientId, Long entrepriseId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé"));
        client.getUser().setArchived(false);
        client.setArchived(false);
        userRepository.save(client.getUser());
        if (entrepriseId != null) {
            entrepriseRepository.findById(entrepriseId).ifPresent(client::addEntreprise);
        }
        client.setCreatedBy(getCurrentUser().getEmail());
        return ResponseEntity.ok(toResponse(clientRepository.save(client)));
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────
    public List<ClientResponse> getAll() {
        User currentUser = getCurrentUser();
        if (isSuperAdmin(currentUser)) {
            return clientRepository.findAllWithEntreprises().stream()
                    .map(this::toResponse).collect(Collectors.toList());
        } else if (isGerant(currentUser)) {
            Entreprise entreprise = entrepriseRepository.findByGerantId(currentUser.getId()).orElse(null);
            if (entreprise == null) return List.of();
            return clientRepository.findByEntrepriseId(entreprise.getId()).stream()
                    .map(this::toResponse).collect(Collectors.toList());
        } else {
            Entreprise entreprise = entrepriseRepository.findByEmployeUserId(currentUser.getId()).orElse(null);
            if (entreprise == null) return List.of();
            return clientRepository.findByEntrepriseId(entreprise.getId()).stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }
    }

    public ClientResponse getById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé"));
        checkOwnership(client, getCurrentUser());
        return toResponse(client);
    }

    public ClientResponse getByUserId(Long userId) {
        Client client = clientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé"));
        return toResponse(client);
    }

    public ResponseEntity<?> create(ClientRequest request) {
        User currentUser = getCurrentUser();
        Entreprise entreprise = resolveEntreprise(currentUser, request.getEntrepriseId());
        Optional<User> existingByEmail = userRepository.findByEmail(request.getEmail());
        if (existingByEmail.isPresent()) {
            User existingUser = existingByEmail.get();
            boolean isClient = existingUser.getRoles().stream().anyMatch(r -> r.getName() == ERole.ROLE_CLIENT);
            if (isClient) return ResponseEntity.badRequest().body(Map.of("error", "ROLE_CLIENT", "message", "Un client possède déjà ce mail."));
            return ResponseEntity.badRequest().body(Map.of("error", "EMAIL_OTHER_ROLE", "message", "Un utilisateur avec un autre rôle possède déjà ce mail."));
        }
        if (request.getNumtel() != null && !request.getNumtel().isBlank()) {
            Optional<Client> existingByTel = clientRepository.findByNumtel(request.getNumtel());
            if (existingByTel.isPresent()) return ResponseEntity.badRequest().body(Map.of("error", "TEL_ALREADY_TAKEN", "message", "Ce numéro de téléphone est déjà utilisé."));
        }
        User userClient = new User();
        userClient.setNom(request.getNom());
        userClient.setPrenom(request.getPrenom());
        userClient.setEmail(request.getEmail());
        userClient.setPassword(passwordEncoder.encode(
                request.getPassword() != null && !request.getPassword().isBlank()
                        ? request.getPassword() : UUID.randomUUID().toString()));
        userClient.setArchived(false);
        Role clientRole = roleRepository.findByName(ERole.ROLE_CLIENT)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Role CLIENT non trouvé"));
        userClient.setRoles(Set.of(clientRole));
        userRepository.save(userClient);
        Client client = new Client();
        client.setUser(userClient);
        client.setNumtel(request.getNumtel());
        client.setArchived(false);
        client.setCreatedBy(currentUser.getEmail());
        if (entreprise != null) client.addEntreprise(entreprise);
        return ResponseEntity.ok(toResponse(clientRepository.save(client)));
    }

    public ClientResponse update(Long id, ClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé"));
        checkOwnership(client, getCurrentUser());
        client.setNumtel(request.getNumtel());
        client.getUser().setNom(request.getNom());
        client.getUser().setPrenom(request.getPrenom());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            client.getUser().setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(client.getUser());
        return toResponse(clientRepository.save(client));
    }

    // ─── ARCHIVER / DÉSARCHIVER ───────────────────────────────────────────────
    @Transactional
    public ClientResponse setArchived(Long id, boolean archived) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé"));

        client.setArchived(archived);
        client.getUser().setArchived(archived);
        userRepository.save(client.getUser());

        if (archived) {
            // Annuler toutes les réservations actives
            reservationRepository.findByClientId(id).stream()
                    .filter(r -> r.getStatut() == StatutReservation.EN_ATTENTE
                            || r.getStatut() == StatutReservation.CONFIRMEE
                            || r.getStatut() == StatutReservation.EN_COURS)
                    .forEach(r -> {
                        r.setStatut(StatutReservation.ANNULEE);
                        reservationRepository.save(r);
                    });

            // Annuler toutes les entrées actives en file d'attente
            fileAttenteRepository.findByClientId(id).stream()
                    .filter(f -> f.getStatut() == StatutFileAttente.EN_ATTENTE
                            || f.getStatut() == StatutFileAttente.APPELE)
                    .forEach(f -> {
                        f.setStatut(StatutFileAttente.ANNULE);
                        fileAttenteRepository.save(f);
                    });

            // Dissocier de toutes les entreprises
            new ArrayList<>(client.getEntreprises()).forEach(client::removeEntreprise);
        }

        return toResponse(clientRepository.save(client));
    }

    // ─── DISSOCIER D'UNE ENTREPRISE ───────────────────────────────────────────
    @Transactional
    public ResponseEntity<?> dissocierDeEntreprise(Long clientId, Long entrepriseId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé"));
        Entreprise entreprise = entrepriseRepository.findById(entrepriseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entreprise non trouvée"));
        boolean associe = client.getEntreprises().stream().anyMatch(e -> e.getId().equals(entrepriseId));
        if (!associe) {
            return ResponseEntity.badRequest().body(Map.of("error", "NOT_ASSOCIATED", "message", "Ce client n'est pas associé à cette entreprise."));
        }
        reservationRepository.findByClientId(clientId).stream()
                .filter(r -> r.getEntreprise() != null && r.getEntreprise().getId().equals(entrepriseId))
                .filter(r -> r.getStatut() == StatutReservation.EN_ATTENTE || r.getStatut() == StatutReservation.CONFIRMEE)
                .forEach(r -> { r.setStatut(StatutReservation.ANNULEE); reservationRepository.save(r); });
        fileAttenteRepository.findByClientId(clientId).stream()
                .filter(f -> f.getEntreprise() != null && f.getEntreprise().getId().equals(entrepriseId))
                .filter(f -> f.getStatut() == StatutFileAttente.EN_ATTENTE || f.getStatut() == StatutFileAttente.APPELE)
                .forEach(f -> { f.setStatut(StatutFileAttente.ANNULE); fileAttenteRepository.save(f); });
        client.removeEntreprise(entreprise);
        clientRepository.save(client);
        return ResponseEntity.ok(Map.of("message", "Client retiré de " + entreprise.getNom()));
    }

    // ─── SUPPRIMER DÉFINITIVEMENT ─────────────────────────────────────────────
    @Transactional
    public void supprimerDefinitivement(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé"));
        List<String> relations = new ArrayList<>();
        if (!client.getEntreprises().isEmpty()) {
            String noms = client.getEntreprises().stream().map(Entreprise::getNom).collect(Collectors.joining(", "));
            relations.add("entreprise(s) : " + noms);
        }
        long nbReservations = reservationRepository.findByClientId(id).size();
        if (nbReservations > 0) relations.add(nbReservations + " réservation(s)");
        long nbFileAttente = fileAttenteRepository.findByClientId(id).size();
        if (nbFileAttente > 0) relations.add(nbFileAttente + " entrée(s) en file d'attente");
        long nbAvis = avisRepository.findByClientId(id).size();
        if (nbAvis > 0) relations.add(nbAvis + " avis");
        if (!relations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ce client est lié à : " + String.join(", ", relations)
                            + ". Supprimez ces relations avant de supprimer le client.");
        }
        clientRepository.delete(client);
        userRepository.delete(client.getUser());
    }

    public void delete(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé"));
        clientRepository.delete(client);
        userRepository.delete(client.getUser());
    }
}