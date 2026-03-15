package com.lounes.gestion_reservations.service;

import com.lounes.gestion_reservations.dto.EmployeCheckResponse;
import com.lounes.gestion_reservations.dto.GerantRequest;
import com.lounes.gestion_reservations.dto.GerantResponse;
import com.lounes.gestion_reservations.model.*;
import com.lounes.gestion_reservations.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GerantService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EntrepriseRepository entrepriseRepository;
    @Autowired private RoleRepository roleRepository;

    public List<GerantResponse> getAll() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream()
                        .anyMatch(r -> r.getName().name().equals("ROLE_GERANT")))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<GerantResponse> getDisponibles() {
        Set<Long> assignedIds = entrepriseRepository.findAll().stream()
                .filter(e -> e.getGerant() != null)
                .map(e -> e.getGerant().getId())
                .collect(Collectors.toSet());

        return userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream()
                        .anyMatch(r -> r.getName().name().equals("ROLE_GERANT")))
                .filter(u -> !assignedIds.contains(u.getId()))
                .filter(u -> !Boolean.TRUE.equals(u.getArchived()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public GerantResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gérant introuvable"));
        return toResponse(user);
    }

    // Vérifier email avant création d'un gérant (mêmes règles que l'employé)
    public EmployeCheckResponse checkEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        // Aucun compte → nouveau
        if (userOpt.isEmpty())
            return new EmployeCheckResponse("NOUVEAU",
                    "Aucun compte trouvé. Vous pouvez créer un nouveau gérant.",
                    null, null, null, email);

        User user = userOpt.get();

        // Vérifier si ce user est GÉRANT
        boolean isGerant = user.getRoles().stream()
                .anyMatch(r -> r.getName() == ERole.ROLE_GERANT);

        if (!isGerant) {
            // Email utilisé par un autre rôle (client, employé, super admin...)
            return new EmployeCheckResponse("EMAIL_OTHER_ROLE",
                    "Cet email est déjà utilisé par un autre type de compte. Veuillez choisir un autre email.",
                    null, null, null, email);
        }

        // Gérant archivé → proposer désarchivage
        if (Boolean.TRUE.equals(user.getArchived())) {
            return new EmployeCheckResponse("LIBRE",
                    "Ce gérant est archivé. Voulez-vous le désarchiver ?",
                    user.getId(), user.getNom(), user.getPrenom(), user.getEmail());
        }

        // Gérant actif avec entreprise → occupé
        Optional<Entreprise> entrepriseOpt = entrepriseRepository.findByGerantId(user.getId());
        if (entrepriseOpt.isPresent()) {
            return new EmployeCheckResponse("OCCUPE",
                    "Ce gérant est déjà assigné à l'entreprise : " + entrepriseOpt.get().getNom(),
                    user.getId(), user.getNom(), user.getPrenom(), user.getEmail());
        }

        // Gérant actif sans entreprise → compte existant, bloquer
        return new EmployeCheckResponse("OCCUPE",
                "Ce mail a déjà un compte gérant actif dans le système.",
                user.getId(), user.getNom(), user.getPrenom(), user.getEmail());
    }

    public GerantResponse update(Long id, GerantRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gérant introuvable"));

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé !");

        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (request.getPassword().length() < 6)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Le mot de passe doit contenir au moins 6 caractères");
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);
        return toResponse(user);
    }

    // Archiver un gérant — remplaçantId obligatoire si le gérant a une entreprise
    public GerantResponse archiver(Long id, Long remplacantId) {
        User gerant = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gérant introuvable"));

        Optional<Entreprise> entrepriseOpt = entrepriseRepository.findByGerantId(id);

        if (entrepriseOpt.isPresent()) {
            if (remplacantId == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Ce gérant a une entreprise. Veuillez choisir un gérant remplaçant.");

            if (remplacantId.equals(id))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Le remplaçant ne peut pas être le même gérant.");

            User remplacant = userRepository.findById(remplacantId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Remplaçant introuvable"));

            if (entrepriseRepository.findByGerantId(remplacantId).isPresent())
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Le gérant remplaçant est déjà assigné à une autre entreprise !");

            Entreprise entreprise = entrepriseOpt.get();
            entreprise.setGerant(remplacant);

            Role roleGerant = roleRepository.findByName(ERole.ROLE_GERANT)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Role GERANT non trouvé"));
            remplacant.getRoles().add(roleGerant);
            remplacant.setArchived(false);
            userRepository.save(remplacant);
            entrepriseRepository.save(entreprise);
        }

        gerant.setArchived(true);
        userRepository.save(gerant);
        return toResponse(gerant);
    }

    public GerantResponse desarchiver(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gérant introuvable"));
        user.setArchived(false);
        userRepository.save(user);
        return toResponse(user);
    }

    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gérant introuvable"));
        userRepository.delete(user);
    }

    private GerantResponse toResponse(User u) {
        GerantResponse r = new GerantResponse(u.getId(), u.getNom(), u.getPrenom(), u.getEmail(), u.getArchived());
        entrepriseRepository.findByGerantId(u.getId()).ifPresent(e -> {
            r.setEntrepriseId(e.getId());
            r.setEntrepriseNom(e.getNom());
            r.setEntrepriseAdresse(e.getAdresse());
            r.setEntrepriseTelephone(e.getTelephone());
            if (e.getSecteur() != null) r.setEntrepriseSecteur(e.getSecteur().getNom());
        });
        return r;
    }
}