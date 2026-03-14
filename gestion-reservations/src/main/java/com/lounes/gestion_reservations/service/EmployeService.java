package com.lounes.gestion_reservations.service;

import com.lounes.gestion_reservations.dto.EmployeRequest;
import com.lounes.gestion_reservations.dto.EmployeResponse;
import com.lounes.gestion_reservations.model.*;
import com.lounes.gestion_reservations.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeService {

    @Autowired private EmployeRepository employeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private EntrepriseRepository entrepriseRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non trouvé"));
    }

    private Entreprise getEntrepriseOfGerant(User gerant) {
        return entrepriseRepository.findByGerantId(gerant.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Aucune entreprise trouvée pour ce gérant"));
    }

    // ─── CHECK EMAIL ──────────────────────────────────────────────────────────
    // Retourne : NOT_FOUND / EMAIL_OTHER_ROLE / FREE / BUSY / ALREADY_IN_THIS_COMPANY / ARCHIVED
    public Map<String, Object> checkEmail(String email, Long entrepriseId) {
        Optional<Employe> empOpt = employeRepository.findByUserEmail(email);

        if (empOpt.isEmpty()) {
            // Vérifier si l'email appartient à un autre rôle
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                return Map.of(
                        "status", "EMAIL_OTHER_ROLE",
                        "message", "Cet email est déjà utilisé par un autre type de compte. Veuillez choisir un autre email."
                );
            }
            return Map.of("status", "NOT_FOUND");
        }

        Employe emp = empOpt.get();
        User user = emp.getUser();

        Map<String, Object> result = new HashMap<>();
        result.put("id", emp.getId());
        result.put("nom", user.getNom());
        result.put("prenom", user.getPrenom());
        result.put("email", user.getEmail());
        result.put("specialite", emp.getSpecialite() != null ? emp.getSpecialite() : "");
        result.put("archived", user.getArchived());

        // Employé archivé → cas spécial
        if (Boolean.TRUE.equals(user.getArchived())) {
            result.put("status", "ARCHIVED");
            return result;
        }

        // Employé actif
        if (emp.getEntreprise() == null) {
            result.put("status", "FREE");
        } else if (entrepriseId != null && emp.getEntreprise().getId().equals(entrepriseId)) {
            result.put("status", "ALREADY_IN_THIS_COMPANY");
            result.put("entrepriseNom", emp.getEntreprise().getNom());
        } else {
            result.put("status", "BUSY");
            result.put("entrepriseNom", emp.getEntreprise().getNom());
        }
        return result;
    }

    // ─── RATTACHER un employé existant (LIBRE) à une entreprise ──────────────
    public Map<String, Object> rattacher(Long userId, Long entrepriseId, String specialite) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));
        Employe emp = employeRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé non trouvé"));
        Entreprise entreprise = entrepriseRepository.findById(entrepriseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entreprise non trouvée"));

        if (emp.getEntreprise() != null && !emp.getEntreprise().getId().equals(entrepriseId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cet employé est déjà rattaché à une autre entreprise : " + emp.getEntreprise().getNom());
        }

        emp.setEntreprise(entreprise);
        if (specialite != null && !specialite.isBlank()) emp.setSpecialite(specialite);
        employeRepository.save(emp);
        return Map.of("message", "Employé rattaché avec succès", "employeId", emp.getId());
    }

    // ─── RATTACHER PAR EMAIL (appelé depuis POST /rattacher) ──────────────────
    public Map<String, Object> rattacherByEmail(String email, Long entrepriseId, String specialite) {
        Employe emp = employeRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Aucun employé trouvé avec l'email : " + email));

        Entreprise entreprise;
        if (entrepriseId != null) {
            entreprise = entrepriseRepository.findById(entrepriseId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entreprise non trouvée"));
        } else {
            entreprise = getEntrepriseOfGerant(getCurrentUser());
        }

        // Si archivé → désarchiver d'abord
        if (Boolean.TRUE.equals(emp.getUser().getArchived())) {
            emp.getUser().setArchived(false);
            userRepository.save(emp.getUser());
        }

        if (emp.getEntreprise() != null && !emp.getEntreprise().getId().equals(entreprise.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cet employé est déjà rattaché à : " + emp.getEntreprise().getNom());
        }

        emp.setEntreprise(entreprise);
        if (specialite != null && !specialite.isBlank()) emp.setSpecialite(specialite);
        Employe saved = employeRepository.save(emp);
        return Map.of("message", "Employé rattaché avec succès", "employe", toResponse(saved));
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────
    public ResponseEntity<?> create(EmployeRequest request) {
        User currentUser = getCurrentUser();

        Entreprise entreprise;
        if (request.getEntrepriseId() != null) {
            entreprise = entrepriseRepository.findById(request.getEntrepriseId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entreprise non trouvée"));
        } else {
            entreprise = getEntrepriseOfGerant(currentUser);
        }

        Optional<Employe> existingEmp = employeRepository.findByUserEmail(request.getEmail());

        if (existingEmp.isPresent()) {
            Employe emp = existingEmp.get();
            if (emp.getEntreprise() == null) {
                emp.setEntreprise(entreprise);
                if (request.getSpecialite() != null && !request.getSpecialite().isBlank()) {
                    emp.setSpecialite(request.getSpecialite());
                }
                return ResponseEntity.ok(toResponse(employeRepository.save(emp)));
            } else if (emp.getEntreprise().getId().equals(entreprise.getId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "ALREADY_IN_THIS_COMPANY",
                                "message", "Cet employé est déjà dans votre entreprise."));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "BUSY",
                                "message", "Cet employé appartient déjà à l'entreprise : "
                                        + emp.getEntreprise().getNom()));
            }
        }

        // Email existe mais pas dans la table employe → autre rôle
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            boolean isEmploye = existingUser.get().getRoles().stream()
                    .anyMatch(r -> r.getName() == ERole.ROLE_EMPLOYE);
            if (isEmploye) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "EMPLOYE_EXISTS",
                        "message", "Un employé avec cet email existe déjà."));
            }
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "EMAIL_OTHER_ROLE",
                    "message", "Cet email est déjà utilisé par un autre type de compte. Veuillez choisir un autre email."));
        }

        User user = new User();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setArchived(false);

        Role role = roleRepository.findByName(ERole.ROLE_EMPLOYE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Role EMPLOYE non trouvé"));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        userRepository.save(user);

        Employe employe = new Employe();
        employe.setUser(user);
        employe.setSpecialite(request.getSpecialite());
        employe.setEntreprise(entreprise);

        return ResponseEntity.ok(toResponse(employeRepository.save(employe)));
    }

    public List<EmployeResponse> getAll() {
        User currentUser = getCurrentUser();
        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName() == ERole.ROLE_SUPER_ADMIN);
        if (isSuperAdmin) {
            return employeRepository.findAll().stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }
        Entreprise entreprise = getEntrepriseOfGerant(currentUser);
        return employeRepository.findByEntreprise(entreprise).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<EmployeResponse> getByEntrepriseId(Long entrepriseId) {
        Entreprise entreprise = entrepriseRepository.findById(entrepriseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entreprise non trouvée"));
        return employeRepository.findByEntreprise(entreprise).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public EmployeResponse getById(Long id) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé non trouvé"));
        return toResponse(employe);
    }

    public EmployeResponse update(Long id, EmployeRequest request) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé non trouvé"));

        User user = employe.getUser();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());

        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email déjà utilisé !");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        employe.setSpecialite(request.getSpecialite());
        userRepository.save(user);
        return toResponse(employeRepository.save(employe));
    }

    public void desactiver(Long id) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé non trouvé"));
        employe.getUser().setArchived(true);
        userRepository.save(employe.getUser());
    }

    public void reactiver(Long id) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé non trouvé"));
        employe.getUser().setArchived(false);
        userRepository.save(employe.getUser());
    }

    public void archiver(Long id) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé non trouvé"));
        employe.getUser().setArchived(true);
        employe.setEntreprise(null);
        userRepository.save(employe.getUser());
        employeRepository.save(employe);
    }

    public void desarchiver(Long id) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé non trouvé"));
        employe.getUser().setArchived(false);
        userRepository.save(employe.getUser());
    }

    private EmployeResponse toResponse(Employe employe) {
        return new EmployeResponse(
                employe.getId(),
                employe.getUser().getNom(),
                employe.getUser().getPrenom(),
                employe.getUser().getEmail(),
                employe.getSpecialite(),
                employe.getUser().getArchived(),
                employe.getEntreprise() != null ? employe.getEntreprise().getNom() : null
        );
    }
}