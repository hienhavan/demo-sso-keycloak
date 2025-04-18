package com.example.demo.application.service;

import com.example.demo.domain.model.User;
import com.example.demo.domain.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Application service for user-related operations.
 * This service uses the domain service to perform operations and adds application-specific logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserDomainService userDomainService;

    /**
     * Get the current authenticated user
     * @return Optional containing the user if authenticated
     */
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof JwtAuthenticationToken) {
            Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
            String keycloakId = jwt.getSubject();
            
            return userDomainService.findByKeycloakId(keycloakId)
                    .or(() -> {
                        // If user doesn't exist in our database yet, create it from Keycloak info
                        String username = jwt.getClaimAsString("preferred_username");
                        String email = jwt.getClaimAsString("email");
                        String firstName = jwt.getClaimAsString("given_name");
                        String lastName = jwt.getClaimAsString("family_name");
                        
                        User newUser = userDomainService.createUserFromKeycloak(
                                keycloakId, username, email, firstName, lastName);
                        
                        return Optional.of(newUser);
                    });
        }
        
        return Optional.empty();
    }

    /**
     * Get a user by ID
     * @param id The user ID
     * @return Optional containing the user if found
     */
    public Optional<User> getUserById(UUID id) {
        return userDomainService.findById(id);
    }

    /**
     * Get a user by Keycloak ID
     * @param keycloakId The Keycloak user ID
     * @return Optional containing the user if found
     */
    public Optional<User> getUserByKeycloakId(String keycloakId) {
        return userDomainService.findByKeycloakId(keycloakId);
    }

    /**
     * Synchronize user information from Keycloak
     * @param keycloakId The Keycloak user ID
     * @param username The username
     * @param email The email address
     * @param firstName The first name
     * @param lastName The last name
     * @return The synchronized user
     */
    public User syncUserFromKeycloak(String keycloakId, String username, String email, String firstName, String lastName) {
        return userDomainService.findOrCreateUserFromKeycloak(keycloakId, username, email, firstName, lastName);
    }
}
