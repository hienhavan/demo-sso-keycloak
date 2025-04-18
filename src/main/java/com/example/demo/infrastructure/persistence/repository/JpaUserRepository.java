package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for UserEntity.
 */
@Repository
public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {
    
    /**
     * Find a user by their Keycloak ID
     * @param keycloakId The Keycloak user ID
     * @return Optional containing the user if found
     */
    Optional<UserEntity> findByKeycloakId(String keycloakId);
    
    /**
     * Find a user by their username
     * @param username The username
     * @return Optional containing the user if found
     */
    Optional<UserEntity> findByUsername(String username);
    
    /**
     * Find a user by their email
     * @param email The email address
     * @return Optional containing the user if found
     */
    Optional<UserEntity> findByEmail(String email);
}
