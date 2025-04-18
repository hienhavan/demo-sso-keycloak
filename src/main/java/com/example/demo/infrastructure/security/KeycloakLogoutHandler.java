package com.example.demo.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Logout handler for Keycloak.
 * Handles the logout process by calling the Keycloak logout endpoint.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KeycloakLogoutHandler implements LogoutHandler {

    private final RestTemplate restTemplate;
    
    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri;
    
    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        logoutFromKeycloak(authentication);
    }
    
    /**
     * Performs a logout request to Keycloak's end-session endpoint
     */
    private void logoutFromKeycloak(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser) {
            OidcUser user = (OidcUser) authentication.getPrincipal();
            
            String endSessionEndpoint = UriComponentsBuilder
                    .fromUriString(issuerUri)
                    .path("/protocol/openid-connect/logout")
                    .queryParam("id_token_hint", user.getIdToken().getTokenValue())
                    .toUriString();
            
            log.info("Logging out from Keycloak: {}", endSessionEndpoint);
            
            try {
                ResponseEntity<String> logoutResponse = restTemplate.getForEntity(endSessionEndpoint, String.class);
                if (logoutResponse.getStatusCode().is2xxSuccessful()) {
                    log.info("Successfully logged out from Keycloak");
                } else {
                    log.warn("Keycloak logout failed: {}", logoutResponse.getStatusCode());
                }
            } catch (Exception e) {
                log.error("Error logging out from Keycloak", e);
            }
        } else {
            log.info("No valid user session to logout from Keycloak");
        }
    }
}
