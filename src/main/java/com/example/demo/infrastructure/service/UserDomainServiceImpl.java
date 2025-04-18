package com.example.demo.infrastructure.service;

import com.example.demo.domain.model.User;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.domain.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDomainServiceImpl implements UserDomainService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User createUserFromKeycloak(String keycloakId, String username, String email, String firstName, String lastName) {
        log.info("Creating new user from Keycloak with ID: {}", keycloakId);
        
        User user = User.builder()
                .id(UUID.randomUUID())
                .keycloakId(keycloakId)
                .username(username)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User findOrCreateUserFromKeycloak(String keycloakId, String username, String email, String firstName, String lastName) {
        log.info("Finding or creating user from Keycloak with ID: {}", keycloakId);
        
        return userRepository.findByKeycloakId(keycloakId)
                .map(existingUser -> {
                    // Update user information if needed
                    boolean updated = false;
                    
                    if (!existingUser.getUsername().equals(username)) {
                        existingUser.setUsername(username);
                        updated = true;
                    }
                    
                    if (!existingUser.getEmail().equals(email)) {
                        existingUser.setEmail(email);
                        updated = true;
                    }
                    
                    if (!existingUser.getFirstName().equals(firstName)) {
                        existingUser.setFirstName(firstName);
                        updated = true;
                    }
                    
                    if (!existingUser.getLastName().equals(lastName)) {
                        existingUser.setLastName(lastName);
                        updated = true;
                    }
                    
                    if (updated) {
                        existingUser.setUpdatedAt(LocalDateTime.now());
                        return userRepository.save(existingUser);
                    }
                    
                    return existingUser;
                })
                .orElseGet(() -> createUserFromKeycloak(keycloakId, username, email, firstName, lastName));
    }

    @Override
    public Optional<User> findByKeycloakId(String keycloakId) {
        log.info("Finding user by Keycloak ID: {}", keycloakId);
        return userRepository.findByKeycloakId(keycloakId);
    }

    @Override
    public Optional<User> findById(UUID id) {
        log.info("Finding user by ID: {}", id);
        return userRepository.findById(id);
    }
}
