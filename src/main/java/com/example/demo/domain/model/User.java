package com.example.demo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User domain entity representing a user in the system.
 * This is the core domain entity that maps to a Keycloak user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String keycloakId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Domain methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isValid() {
        return username != null && !username.isEmpty() 
                && email != null && !email.isEmpty()
                && keycloakId != null && !keycloakId.isEmpty();
    }
}
