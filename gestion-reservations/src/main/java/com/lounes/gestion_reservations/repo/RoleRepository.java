package com.lounes.gestion_reservations.repo;

import com.lounes.gestion_reservations.model.ERole;
import com.lounes.gestion_reservations.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}