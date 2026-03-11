package com.lounes.gestion_reservations.controller;

import com.lounes.gestion_reservations.dto.JwtResponse;
import com.lounes.gestion_reservations.dto.LoginRequest;
import com.lounes.gestion_reservations.dto.SignupRequest;
import com.lounes.gestion_reservations.model.*;
import com.lounes.gestion_reservations.repo.ClientRepository;
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

import java.util.HashSet;
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
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtils jwtUtils;

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
        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(),
                userDetails.getUsername(), user.getNom(), user.getPrenom(), roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail()))
            return ResponseEntity.badRequest().body("Email déjà utilisé !");

        User user = new User();
        user.setNom(signupRequest.getNom());
        user.setPrenom(signupRequest.getPrenom());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setArchived(false);   // ← remplace setActif(true)

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
                "id", savedUser.getId(),
                "email", savedUser.getEmail()));
    }

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
        user.setArchived(false);   // ← remplace setActif(true)

        Role gerantRole = roleRepository.findByName(ERole.ROLE_GERANT)
                .orElseThrow(() -> new RuntimeException("Role GERANT non trouvé"));
        user.setRoles(Set.of(gerantRole));
        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Compte gérant créé avec succès !",
                "id", savedUser.getId(),
                "email", savedUser.getEmail()));
    }
}