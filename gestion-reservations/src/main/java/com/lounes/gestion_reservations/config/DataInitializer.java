package com.lounes.gestion_reservations.config;

import com.lounes.gestion_reservations.model.ERole;
import com.lounes.gestion_reservations.model.Role;
import com.lounes.gestion_reservations.model.User;
import com.lounes.gestion_reservations.repo.RoleRepository;
import com.lounes.gestion_reservations.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        for (ERole eRole : ERole.values()) {
            if (roleRepository.findByName(eRole).isEmpty()) {
                Role role = new Role();
                role.setName(eRole);
                roleRepository.save(role);
                System.out.println("Rôle créé : " + eRole.name());
            }
        }

        String adminEmail = "superadmin@gmail.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            Role superAdminRole = roleRepository.findByName(ERole.ROLE_SUPER_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Role SUPER_ADMIN non trouvé"));
            User admin = new User();
            admin.setNom("Admin");
            admin.setPrenom("Super");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setArchived(false);
            admin.setRoles(Set.of(superAdminRole));
            userRepository.save(admin);
            System.out.println("Super Admin créé : " + adminEmail);
        }
    }
}