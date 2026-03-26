package com.lounes.gestion_reservations.controller;

import com.lounes.gestion_reservations.dto.JwtResponse;
import com.lounes.gestion_reservations.dto.LoginRequest;
import com.lounes.gestion_reservations.dto.SignupRequest;
import com.lounes.gestion_reservations.model.*;
import com.lounes.gestion_reservations.repo.ClientRepository;
import com.lounes.gestion_reservations.repo.EmployeRepository;
import com.lounes.gestion_reservations.repo.EntrepriseRepository;
import com.lounes.gestion_reservations.repo.RoleRepository;
import com.lounes.gestion_reservations.repo.UserRepository;
import com.lounes.gestion_reservations.security.JwtUtils;
import com.lounes.gestion_reservations.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private EmployeRepository employeRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private EntrepriseRepository entrepriseRepository;

    // ─── LOGIN ────────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority()).collect(Collectors.toList());
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long entrepriseId = resolveEntrepriseId(user, roles);

        // ── User archivé au niveau User (tous rôles sauf SUPER_ADMIN) ──────────
        if (Boolean.TRUE.equals(user.getArchived()) && !roles.contains("ROLE_SUPER_ADMIN")) {
            String reason = roles.contains("ROLE_GERANT")  ? "GERANT_ARCHIVE"  :
                    roles.contains("ROLE_EMPLOYE") ? "EMPLOYE_ARCHIVE" :
                            roles.contains("ROLE_CLIENT")  ? "CLIENT_ARCHIVE"  : "COMPTE_ARCHIVE";
            return ResponseEntity.status(403).body(Map.of(
                    "reason",  reason,
                    "message", "Votre compte a été archivé. Contactez l'administrateur."
            ));
        }

        // ── Gérant sans entreprise (libre) ──────────────────────────────────────
        if (roles.contains("ROLE_GERANT") && entrepriseId == null) {
            return ResponseEntity.status(403).body(Map.of(
                    "reason", "GERANT_SANS_ENTREPRISE",
                    "message", "Votre compte gérant n'est pas encore associé à une entreprise."
            ));
        }

        // ── Employé archivé (table employes) ou sans entreprise ────────────────
        if (roles.contains("ROLE_EMPLOYE")) {
            var employe = employeRepository.findByUserId(user.getId()).orElse(null);
            if (employe != null && Boolean.TRUE.equals(employe.getArchived())) {
                return ResponseEntity.status(403).body(Map.of(
                        "reason", "EMPLOYE_ARCHIVE",
                        "message", "Votre compte employé a été archivé. Contactez l'administrateur."
                ));
            }
            if (employe == null || employe.getEntreprise() == null) {
                return ResponseEntity.status(403).body(Map.of(
                        "reason", "EMPLOYE_SANS_ENTREPRISE",
                        "message", "Votre compte employé n'est rattaché à aucune entreprise."
                ));
            }
        }

        // ── Client archivé (table clients) ─────────────────────────────────────
        if (roles.contains("ROLE_CLIENT")) {
            var client = clientRepository.findByUserId(user.getId()).orElse(null);
            if (client != null && Boolean.TRUE.equals(client.getArchived())) {
                return ResponseEntity.status(403).body(Map.of(
                        "reason", "CLIENT_ARCHIVE",
                        "message", "Votre compte client a été archivé. Contactez l'administrateur."
                ));
            }
        }

        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(),
                userDetails.getUsername(), user.getNom(), user.getPrenom(), roles, entrepriseId));
    }

    // ─── REFRESH PROFILE ─────────────────────────────────────────────────────
    // Permet au gérant de récupérer son entrepriseId à jour sans se reconnecter
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMe() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());
        Long entrepriseId = resolveEntrepriseId(user, roles);

        return ResponseEntity.ok(Map.of(
                "id",           user.getId(),
                "email",        user.getEmail(),
                "nom",          user.getNom(),
                "prenom",       user.getPrenom(),
                "roles",        roles,
                "entrepriseId", entrepriseId != null ? entrepriseId : ""
        ));
    }

    // ─── SIGNUP ───────────────────────────────────────────────────────────────
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail()))
            return ResponseEntity.badRequest().body("Email déjà utilisé !");

        User user = new User();
        user.setNom(signupRequest.getNom());
        user.setPrenom(signupRequest.getPrenom());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setArchived(false);

        Role clientRole = roleRepository.findByName(ERole.ROLE_CLIENT)
                .orElseThrow(() -> new RuntimeException("Role CLIENT non trouvé"));
        user.setRoles(Set.of(clientRole));
        User savedUser = userRepository.save(user);

        Client client = new Client();
        client.setUser(savedUser);
        client.setNumtel(signupRequest.getNumtel());
        client.setArchived(false);
        clientRepository.save(client);

        return ResponseEntity.ok(Map.of(
                "message", "Compte client créé avec succès !",
                "id",      savedUser.getId(),
                "email",   savedUser.getEmail()));
    }

    // ─── CREATE GÉRANT ────────────────────────────────────────────────────────
    @PostMapping("/create-gerant")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createGerant(@Valid @RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail()))
            return ResponseEntity.badRequest().body("Email déjà utilisé !");

        User user = new User();
        user.setNom(signupRequest.getNom());
        user.setPrenom(signupRequest.getPrenom());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setArchived(false);

        Role gerantRole = roleRepository.findByName(ERole.ROLE_GERANT)
                .orElseThrow(() -> new RuntimeException("Role GERANT non trouvé"));
        user.setRoles(Set.of(gerantRole));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Compte gérant créé avec succès !",
                "id",      user.getId(),
                "email",   user.getEmail()));
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────
    private Long resolveEntrepriseId(User user, List<String> roles) {
        if (roles.contains("ROLE_GERANT")) {
            return entrepriseRepository.findByGerantId(user.getId())
                    .map(e -> e.getId())
                    .orElse(null);
        }
        return null;
    }
}