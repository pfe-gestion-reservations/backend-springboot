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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeService {

    @Autowired private EmployeRepository employeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private EntrepriseRepository entrepriseRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private FileAttenteRepository fileAttenteRepository;
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

    // ─── CHECK EMAIL : recherche directe dans la table employe ──────────────
    // Retourne statut FREE / BUSY / ALREADY_IN_THIS_COMPANY / NOT_FOUND
    public Map<String, Object> checkEmail(String email, Long entrepriseId) {
        // Recherche DIRECTE dans la table employe (pas users)
        Optional<Employe> empOpt = employeRepository.findByUserEmail(email);

        if (empOpt.isEmpty()) {
            // Vérifier si l'email est utilisé par un autre rôle (SUPER_ADMIN, GÉRANT, CLIENT...)
            if (userRepository.existsByEmail(email)) {
                return Map.of(
                        "status", "EMAIL_OTHER_ROLE",
                        "message", "Cet email est déjà utilisé par un compte d'un autre rôle."
                );
            }
            // Aucun compte → formulaire création
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

        // ── Archivé → priorité absolue, peu importe l'entreprise ──────────────
        if (Boolean.TRUE.equals(user.getArchived())) {
            result.put("status", "FREE");  // front détecte archived=true → result-archived
            return result;
        }

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
    @Transactional
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
    @Transactional
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

        if (emp.getEntreprise() != null && !emp.getUser().getArchived()) {
            if (emp.getEntreprise().getId().equals(entreprise.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Cet employé est déjà dans votre entreprise.");
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cet employé est déjà rattaché à : " + emp.getEntreprise().getNom());
        }

        emp.setEntreprise(entreprise);
        if (specialite != null && !specialite.isBlank()) emp.setSpecialite(specialite);
        Employe saved = employeRepository.save(emp);
        return Map.of("message", "Employé rattaché avec succès", "employe", toResponse(saved));
    }

    // ─── CREATE : crée ou associe selon l'état ────────────────────────────────
    public ResponseEntity<?> create(EmployeRequest request) {
        User currentUser = getCurrentUser();

        // Déterminer l'entreprise cible
        Entreprise entreprise;
        if (request.getEntrepriseId() != null) {
            // Super Admin fournit l'ID entreprise
            entreprise = entrepriseRepository.findById(request.getEntrepriseId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entreprise non trouvée"));
        } else {
            // Gérant → son entreprise
            entreprise = getEntrepriseOfGerant(currentUser);
        }

        // Recherche DIRECTE dans la table employe par email
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

        // Aucun enregistrement dans employe → vérifier si email utilisé ailleurs
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "EMAIL_EXISTS_OTHER_ROLE",
                    "message", "Cet email est utilisé par un autre type de compte."));
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
            // SA sans filtre → uniquement les employés rattachés à une entreprise
            return employeRepository.findAll().stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }
        Entreprise entreprise = getEntrepriseOfGerant(currentUser);
        return employeRepository.findByEntreprise(entreprise)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeResponse> getByEntrepriseId(Long entrepriseId) {
        Entreprise entreprise = entrepriseRepository.findById(entrepriseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entreprise non trouvée"));
        return employeRepository.findByEntreprise(entreprise)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EmployeResponse getById(Long id) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé non trouvé"));
        return toResponse(employe);
    }

    @Transactional
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
        employe.getUser().setArchived(false);
        userRepository.save(employe.getUser());
    }

    public void reactiver(Long id) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé non trouvé"));
        employe.getUser().setArchived(true);
        userRepository.save(employe.getUser());
    }

    @Transactional
    public void archiver(Long id) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé non trouvé"));
        employe.getUser().setArchived(true);
        employe.setEntreprise(null); // ← libère l'employé
        userRepository.save(employe.getUser());
        employeRepository.save(employe);
    }

    @Transactional
    public void desarchiver(Long id) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé non trouvé"));
        employe.getUser().setArchived(false);
        userRepository.save(employe.getUser());
        employeRepository.save(employe);
    }

    // ─── DÉSARCHIVER + RATTACHER à l'entreprise du gérant connecté ──────────
    @Transactional
    public void desarchiverEtRattacher(Long id) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé non trouvé"));
        employe.getUser().setArchived(false);
        userRepository.save(employe.getUser());
        // Gérant → rattache à son entreprise | Super admin → désarchive seulement
        User currentUser = getCurrentUser();
        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName() == ERole.ROLE_SUPER_ADMIN);
        if (!isSuperAdmin) {
            Entreprise entreprise = getEntrepriseOfGerant(currentUser);
            employe.setEntreprise(entreprise);
        }
        employeRepository.save(employe);
    }

    // ─── SUPPRESSION DÉFINITIVE ──────────────────────────────────────────────
    @Transactional
    public void supprimerDefinitivement(Long id) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé non trouvé"));

        List<String> relations = new ArrayList<>();

        // 1. Rattaché à une entreprise ?
        if (employe.getEntreprise() != null) {
            relations.add("entreprise \"" + employe.getEntreprise().getNom() + "\"");
        }

        // 2. Réservations liées ?
        long nbReservations = reservationRepository.findByEmployeId(id).size();
        if (nbReservations > 0) {
            relations.add(nbReservations + " réservation(s)");
        }

        // 3. File d'attente liée ?
        long nbFileAttente = fileAttenteRepository.findByEmployeAndStatutNot(
                employe, com.lounes.gestion_reservations.model.StatutFileAttente.TERMINE).size();
        if (nbFileAttente > 0) {
            relations.add(nbFileAttente + " entrée(s) en file d'attente");
        }

        if (!relations.isEmpty()) {
            String detail = String.join(", ", relations);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cet employé est lié à : " + detail + ". Supprimez ces relations avant de supprimer l'employé.");
        }

        // Aucune relation → suppression complète
        Long userId = employe.getUser().getId();
        employeRepository.delete(employe);
        userRepository.deleteById(userId);
    }

    private EmployeResponse toResponse(Employe employe) {
        Long entrepriseId   = employe.getEntreprise() != null ? employe.getEntreprise().getId()  : null;
        String entrepriseNom = employe.getEntreprise() != null ? employe.getEntreprise().getNom() : null;
        return new EmployeResponse(
                employe.getId(),
                employe.getUser().getNom(),
                employe.getUser().getPrenom(),
                employe.getUser().getEmail(),
                employe.getSpecialite(),
                employe.getUser().getArchived(),
                entrepriseId,
                entrepriseNom
        );
    }


}