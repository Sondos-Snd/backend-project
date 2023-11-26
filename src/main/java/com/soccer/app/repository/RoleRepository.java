package com.soccer.app.repository;

import java.util.Optional;

import com.soccer.app.model.ERole;
import com.soccer.app.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
	Optional<Role> findByName(ERole name);
}
