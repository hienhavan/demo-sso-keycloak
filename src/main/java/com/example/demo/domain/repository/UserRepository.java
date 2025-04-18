package com.example.demo.domain.repository;

import com.example.demo.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User domain entity.
 * Following DDD principles, this interface is defined in the domain layer
 * but implemented in the infrastructure layer.
 */
public interface UserRepository {
    
    /**
     * Find a user by their ID
     * @param id The user ID
     * @return Optional containing the user if found
     */
    Optional<User> findById(UUID id);
    
    /**
     * Find a user by their Keycloak ID
     * @param keycloakId The Keycloak user ID
     * @return Optional containing the user if found
     */
    Optional<User> findByKeycloakId(String keycloakId);
    
    /**
     * Find a user by their username
     * @param username The username
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find a user by their email
     * @param email The email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Save a user
     * @param user The user to save
     * @return The saved user
     */
    User save(User user);
    
    /**
     * Find all users
     * @return List of all users
     */
    List<User> findAll();
    
    /**
     * Delete a user
     * @param id The ID of the user to delete
     */
    void deleteById(UUID id);
}
