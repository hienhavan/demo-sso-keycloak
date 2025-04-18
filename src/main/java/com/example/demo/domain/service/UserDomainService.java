package com.example.demo.domain.service;

import com.example.demo.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain service for user-related business logic.
 * In DDD, domain services contain business logic that doesn't naturally fit within an entity.
 */
public interface UserDomainService {
    
    /**
     * Create a new user from Keycloak user information
     * @param keycloakId The Keycloak user ID
     * @param username The username
     * @param email The email address
     * @param firstName The first name
     * @param lastName The last name
     * @return The created user
     */
    User createUserFromKeycloak(String keycloakId, String username, String email, String firstName, String lastName);
    
    /**
     * Find or create a user based on Keycloak information
     * @param keycloakId The Keycloak user ID
     * @param username The username
     * @param email The email address
     * @param firstName The first name
     * @param lastName The last name
     * @return The found or created user
     */
    User findOrCreateUserFromKeycloak(String keycloakId, String username, String email, String firstName, String lastName);
    
    /**
     * Find a user by Keycloak ID
     * @param keycloakId The Keycloak user ID
     * @return Optional containing the user if found
     */
    Optional<User> findByKeycloakId(String keycloakId);
    
    /**
     * Find a user by ID
     * @param id The user ID
     * @return Optional containing the user if found
     */
    Optional<User> findById(UUID id);
}
