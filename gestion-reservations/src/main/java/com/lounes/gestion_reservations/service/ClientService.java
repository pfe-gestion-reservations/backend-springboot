package com.lounes.gestion_reservations.service;

import com.lounes.gestion_reservations.dto.ClientRequest;
import com.lounes.gestion_reservations.dto.ClientResponse;
import com.lounes.gestion_reservations.model.Client;
import com.lounes.gestion_reservations.model.ERole;
import com.lounes.gestion_reservations.model.Employe;
import com.lounes.gestion_reservations.model.Role;
import com.lounes.gestion_reservations.model.User;
import com.lounes.gestion_reservations.repo.ClientRepository;
import com.lounes.gestion_reservations.repo.EmployeRepository;
import com.lounes.gestion_reservations.repo.EntrepriseRepository;
import com.lounes.gestion_reservations.repo.RoleRepository;
import com.lounes.gestion_reservations.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClientService {

    @Autowired private ClientRepository clientRepository;
    @Autowired private EmployeRepository employeRepository;
    @Autowired private EntrepriseRepository entrepriseRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // ───── Utilitaires ─────────────────────────────────────

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

    private ClientResponse toResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getUser().getNom(),
                client.getUser().getPrenom(),
                client.getUser().getEmail(),
                client.getNumtel(),
                client.getArchived(),
                client.getCreatedBy()
        );
    }

    // ───── CRUD ────────────────────────────────────────────

    public List<ClientResponse> getAll() {
        User currentUser = getCurrentUser();
        if (isGerantOrSuperAdmin(currentUser)) {
            return clientRepository.findAll().stream()
                    .map(this::toResponse).collect(Collectors.toList());
        } else {
            Employe employe = getEmployeFromUser(currentUser);
            return clientRepository.findByCreatedBy(employe.getUser().getEmail()).stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }
    }

    public List<ClientResponse> getActifs() {
        return clientRepository.findByArchivedFalse().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<ClientResponse> getArchives() {
        return clientRepository.findByArchivedTrue().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public ClientResponse getById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé"));
        checkOwnership(client, getCurrentUser());
        return toResponse(client);
    }

    public ResponseEntity<?> create(ClientRequest request) {
        Optional<User> existing = userRepository.findByEmail(request.getEmail());
        if (existing.isPresent()) {
            Optional<Client> archivedClient = clientRepository.findByUserIdAndArchivedTrue(existing.get().getId());
            if (archivedClient.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "ALREADY_ARCHIVED",
                        "message", "Un client avec cet email existe déjà mais est archivé.",
                        "clientId", archivedClient.get().getId()
                ));
            }
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "EMAIL_EXISTS",
                    "message", "Un client avec cet email existe déjà."
            ));
        }

        User currentUser = getCurrentUser();
        User userClient = new User();
        userClient.setNom(request.getNom());
        userClient.setPrenom(request.getPrenom());
        userClient.setEmail(request.getEmail());
        userClient.setPassword(passwordEncoder.encode(
                request.getPassword() != null && !request.getPassword().isBlank()
                        ? request.getPassword()
                        : java.util.UUID.randomUUID().toString()
        ));
        userClient.setArchived(false);

        Role clientRole = roleRepository.findByName(ERole.ROLE_CLIENT)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Role CLIENT non trouvé"));
        userClient.setRoles(Set.of(clientRole));
        userRepository.save(userClient);

        Client client = new Client();
        client.setUser(userClient);
        client.setNumtel(request.getNumtel());
        client.setArchived(false);
        client.setCreatedBy(currentUser.getEmail());

        // Assigner l'entreprise du gérant ou de l'employé
        if (isGerant(currentUser)) {
            entrepriseRepository.findByGerantId(currentUser.getId())
                    .ifPresent(client::setEntreprise);
        } else if (!isSuperAdmin(currentUser)) {
            employeRepository.findByUser(currentUser)
                    .ifPresent(emp -> client.setEntreprise(emp.getEntreprise()));
        }

        clientRepository.save(client);

        return ResponseEntity.ok(toResponse(client));
    }

    public ClientResponse update(Long id, ClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé"));
        checkOwnership(client, getCurrentUser());
        client.setNumtel(request.getNumtel());
        client.getUser().setNom(request.getNom());
        client.getUser().setPrenom(request.getPrenom());
        userRepository.save(client.getUser());
        clientRepository.save(client);
        return toResponse(client);
    }

    public ClientResponse setArchived(Long id, boolean archived) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé"));
        client.setArchived(archived);
        client.getUser().setArchived(archived);
        userRepository.save(client.getUser());
        clientRepository.save(client);
        return toResponse(client);
    }

    public void delete(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé"));
        clientRepository.delete(client);
        userRepository.delete(client.getUser());
    }
}