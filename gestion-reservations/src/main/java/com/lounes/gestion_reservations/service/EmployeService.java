package com.lounes.gestion_reservations.service;

import com.lounes.gestion_reservations.dto.EmployeCheckResponse;
import com.lounes.gestion_reservations.dto.EmployeRequest;
import com.lounes.gestion_reservations.dto.EmployeResponse;
import com.lounes.gestion_reservations.model.*;
import com.lounes.gestion_reservations.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmployeService {

    @Autowired private EmployeRepository employeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private EntrepriseRepository entrepriseRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // ── Utilitaires ──────────────────────────────────────────────────────────

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non trouvé"));
    }

    private Entreprise getEntrepriseOfCurrentUser() {
        User u = getCurrentUser();
        // Super admin doit passer entrepriseId explicitement
        return entrepriseRepository.findByGerantId(u.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Aucune entreprise trouvée pour ce gérant"));
    }

    private Entreprise getEntrepriseById(Long id) {
        return entrepriseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entreprise non trouvée"));
    }

    // ── CHECK avant création ─────────────────────────────────────────────────
    // Vérifie si un email existe déjà et quel est son statut
    public EmployeCheckResponse checkEmail(String email, Long entrepriseId) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            // Inconnu → créer un nouveau compte
            return new EmployeCheckResponse("NOUVEAU",
                    "Aucun compte trouvé. Vous pouvez créer un nouvel employé.",
                    null, null, null, email);
        }

        User user = userOpt.get();

        // Chercher profil employé actif (non archivé)
        Optional<Employe> employeActifOpt = employeRepository.findByUserIdAndArchivedFalse(user.getId());

        if (employeActifOpt.isPresent()) {
            Employe emp = employeActifOpt.get();
            // Vérifier si c'est dans la même entreprise
            if (emp.getEntreprise().getId().equals(entrepriseId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Cet employé appartient déjà à cette entreprise !");
            }
            // Dans une autre entreprise active
            return new EmployeCheckResponse("OCCUPE",
                    "Cet employé appartient déjà à une entreprise active : " + emp.getEntreprise().getNom(),
                    user.getId(), user.getNom(), user.getPrenom(), user.getEmail());
        }

        // Pas d'emploi actif → libre (archivé ou jamais employé)
        return new EmployeCheckResponse("LIBRE",
                "Ce compte existe déjà. Voulez-vous ajouter cet employé à votre entreprise ?",
                user.getId(), user.getNom(), user.getPrenom(), user.getEmail());
    }

    // ── CRÉER — nouveau compte ───────────────────────────────────────────────
    public EmployeResponse create(EmployeRequest request, Long entrepriseId) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email déjà utilisé !");

        Entreprise entreprise = getEntrepriseById(entrepriseId);

        User user = new User();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setArchived(false);

        Role role = roleRepository.findByName(ERole.ROLE_EMPLOYE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Role EMPLOYE non trouvé"));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        userRepository.save(user);

        Employe employe = new Employe();
        employe.setUser(user);
        employe.setSpecialite(request.getSpecialite());
        employe.setEntreprise(entreprise);
        employe.setArchived(false);

        return toResponse(employeRepository.save(employe));
    }

    // ── RATTACHER — compte existant libre ───────────────────────────────────
    // Crée un nouveau profil Employe pour un user existant dans une nouvelle entreprise
    public EmployeResponse rattacher(Long userId, Long entrepriseId, String specialite) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

        Entreprise entreprise = getEntrepriseById(entrepriseId);

        // Vérifier qu'il n'est pas déjà actif dans cette entreprise
        boolean dejaActif = employeRepository.findAllByUserId(userId).stream()
                .anyMatch(e -> e.getEntreprise().getId().equals(entrepriseId) && !e.getArchived());
        if (dejaActif)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet employé est déjà actif dans cette entreprise !");

        // Assigner le rôle EMPLOYE si pas encore
        Role role = roleRepository.findByName(ERole.ROLE_EMPLOYE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Role EMPLOYE non trouvé"));
        user.getRoles().add(role);
        user.setArchived(false);
        userRepository.save(user);

        Employe employe = new Employe();
        employe.setUser(user);
        employe.setSpecialite(specialite);
        employe.setEntreprise(entreprise);
        employe.setArchived(false);

        return toResponse(employeRepository.save(employe));
    }

    // ── LIRE ─────────────────────────────────────────────────────────────────

    public List<EmployeResponse> getAll() {
        Entreprise entreprise = getEntrepriseOfCurrentUser();
        return employeRepository.findByEntreprise(entreprise).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<EmployeResponse> getByEntrepriseId(Long entrepriseId) {
        Entreprise entreprise = getEntrepriseById(entrepriseId);
        return employeRepository.findByEntreprise(entreprise).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public EmployeResponse getById(Long id) {
        return toResponse(employeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé non trouvé")));
    }

    // ── MODIFIER ─────────────────────────────────────────────────────────────

    public EmployeResponse update(Long id, EmployeRequest request) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé non trouvé"));

        User user = employe.getUser();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());

        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email déjà utilisé !");
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank())
            user.setPassword(passwordEncoder.encode(request.getPassword()));

        employe.setSpecialite(request.getSpecialite());
        userRepository.save(user);
        return toResponse(employeRepository.save(employe));
    }

    // ── ARCHIVER / DÉSARCHIVER ───────────────────────────────────────────────
    // Archiver = employé quitte l'entreprise → devient libre
    public EmployeResponse archiver(Long id) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé non trouvé"));
        employe.setArchived(true);
        // On n'archive PAS le user — il garde son compte pour d'autres entreprises
        return toResponse(employeRepository.save(employe));
    }

    public EmployeResponse desarchiver(Long id) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé non trouvé"));
        employe.setArchived(false);
        employe.getUser().setArchived(false);
        userRepository.save(employe.getUser());
        return toResponse(employeRepository.save(employe));
    }

    // ── MAPPING ──────────────────────────────────────────────────────────────

    private EmployeResponse toResponse(Employe employe) {
        return new EmployeResponse(
                employe.getId(),
                employe.getUser().getNom(),
                employe.getUser().getPrenom(),
                employe.getUser().getEmail(),
                employe.getSpecialite(),
                employe.getArchived()
        );
    }
}