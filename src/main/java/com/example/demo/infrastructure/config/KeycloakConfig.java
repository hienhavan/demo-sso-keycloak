package com.example.demo.infrastructure.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Cấu hình cho Keycloak SSO.
 * 
 * Lớp này cung cấp các thuộc tính và bean cần thiết để tương tác với Keycloak.
 * Bao gồm các thông tin như realm, client-id, và các endpoint của Keycloak.
 */
@Configuration
@RequiredArgsConstructor
public class KeycloakConfig {

    private final RestTemplate restTemplate;

    @Value("${keycloak.realm}")
    @Getter
    private String realm;

    @Value("${keycloak.auth-server-url}")
    @Getter
    private String authServerUrl;

    @Value("${keycloak.resource}")
    @Getter
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    @Getter
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    @Getter
    private String issuerUri;

    /**
     * Lấy URL endpoint token của Keycloak.
     * Endpoint này được sử dụng để lấy, làm mới và thu hồi token.
     * 
     * @return URL endpoint token
     */
    public String getTokenEndpoint() {
        return issuerUri + "/protocol/openid-connect/token";
    }

    /**
     * Lấy URL endpoint logout của Keycloak.
     * Endpoint này được sử dụng để đăng xuất người dùng khỏi phiên SSO.
     * 
     * @return URL endpoint logout
     */
    public String getLogoutEndpoint() {
        return issuerUri + "/protocol/openid-connect/logout";
    }

    /**
     * Lấy URL endpoint userinfo của Keycloak.
     * Endpoint này được sử dụng để lấy thông tin người dùng từ token.
     * 
     * @return URL endpoint userinfo
     */
    public String getUserInfoEndpoint() {
        return issuerUri + "/protocol/openid-connect/userinfo";
    }

    /**
     * Bean cung cấp thông tin về các endpoint của Keycloak.
     * Hữu ích khi cần truy cập các endpoint từ các thành phần khác.
     * 
     * @return Đối tượng KeycloakEndpoints chứa thông tin về các endpoint
     */
    @Bean
    public KeycloakEndpoints keycloakEndpoints() {
        return new KeycloakEndpoints(
                getTokenEndpoint(),
                getLogoutEndpoint(),
                getUserInfoEndpoint(),
                issuerUri + "/protocol/openid-connect/auth",
                issuerUri + "/protocol/openid-connect/certs"
        );
    }

    /**
     * Lấy token admin từ Keycloak.
     * Token này có thể được sử dụng để gọi Admin API của Keycloak.
     * 
     * @param username Username của admin
     * @param password Password của admin
     * @return Token admin hoặc null nếu không lấy được
     */
    public String getAdminToken(String username, String password) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_id", "admin-cli");
            map.add("username", username);
            map.add("password", password);
            map.add("grant_type", "password");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<AdminTokenResponse> response = restTemplate.postForEntity(
                    authServerUrl + "/realms/master/protocol/openid-connect/token",
                    request,
                    AdminTokenResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getAccessToken();
            }
        } catch (Exception e) {
            // Log lỗi nếu cần
        }
        return null;
    }

    /**
     * Lớp chứa thông tin về các endpoint của Keycloak.
     */
    @Getter
    public static class KeycloakEndpoints {
        private final String tokenEndpoint;
        private final String logoutEndpoint;
        private final String userInfoEndpoint;
        private final String authorizationEndpoint;
        private final String jwksEndpoint;

        public KeycloakEndpoints(
                String tokenEndpoint,
                String logoutEndpoint,
                String userInfoEndpoint,
                String authorizationEndpoint,
                String jwksEndpoint
        ) {
            this.tokenEndpoint = tokenEndpoint;
            this.logoutEndpoint = logoutEndpoint;
            this.userInfoEndpoint = userInfoEndpoint;
            this.authorizationEndpoint = authorizationEndpoint;
            this.jwksEndpoint = jwksEndpoint;
        }
    }

    /**
     * Lớp đại diện cho phản hồi token từ Keycloak.
     * Chỉ lưu trữ và sử dụng accessToken, các trường khác được Jackson deserialize
     * nhưng không cần sử dụng trong ứng dụng.
     */
    private static class AdminTokenResponse {
        private String accessToken;
        
        // Chỉ cần getter cho accessToken vì chỉ nó được sử dụng
        public String getAccessToken() {
            return accessToken;
        }
        
        // Setter cần thiết cho Jackson deserialize
        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}
