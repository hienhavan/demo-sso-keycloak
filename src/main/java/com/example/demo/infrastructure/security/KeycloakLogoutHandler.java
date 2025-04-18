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
 * Handler xử lý logout từ Keycloak.
 * 
 * Lớp này triển khai LogoutHandler của Spring Security để xử lý quá trình đăng xuất
 * từ cả ứng dụng và Keycloak SSO. Khi người dùng đăng xuất khỏi ứng dụng, handler này
 * sẽ gửi request đến endpoint logout của Keycloak để đảm bảo phiên SSO cũng được kết thúc.
 * 
 * Điều này đảm bảo rằng khi người dùng đăng xuất khỏi một ứng dụng trong hệ sinh thái SSO,
 * họ cũng sẽ được đăng xuất khỏi Keycloak và tất cả các ứng dụng khác sử dụng cùng phiên SSO.
 */
@Component // Đánh dấu là một component Spring để tự động đăng ký
@Slf4j // Tự động tạo logger với tên lớp (Lombok)
@RequiredArgsConstructor // Tự động tạo constructor với các trường final (Lombok)
public class KeycloakLogoutHandler implements LogoutHandler {

    private final RestTemplate restTemplate; // Dùng để gửi HTTP requests đến Keycloak
    
    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri; // URI của Keycloak realm từ cấu hình
    
    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId; // Client ID từ cấu hình
    
    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret; // Client secret từ cấu hình

    /**
     * Xử lý sự kiện logout từ Spring Security.
     * 
     * Phương thức này được gọi khi người dùng đăng xuất khỏi ứng dụng.
     * Nó sẽ gọi đến Keycloak để đăng xuất người dùng khỏi phiên SSO.
     * 
     * @param request HttpServletRequest từ request logout
     * @param response HttpServletResponse để trả về client
     * @param authentication Đối tượng Authentication chứa thông tin người dùng hiện tại
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log.debug("Logout request received, processing Keycloak logout");
        logoutFromKeycloak(authentication);
    }
    
    /**
     * Thực hiện request logout đến endpoint kết thúc phiên của Keycloak.
     * 
     * Phương thức này gửi một request đến endpoint logout của Keycloak để kết thúc phiên SSO.
     * Nó sử dụng id_token_hint để Keycloak có thể xác định phiên cần kết thúc.
     * 
     * @param authentication Đối tượng Authentication chứa thông tin người dùng hiện tại
     */
    private void logoutFromKeycloak(Authentication authentication) {
        // Kiểm tra nếu người dùng đã xác thực và là OidcUser (OpenID Connect User)
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser) {
            OidcUser user = (OidcUser) authentication.getPrincipal();
            
            // Xây dựng URL logout của Keycloak với id_token_hint
            // id_token_hint giúp Keycloak xác định phiên cần kết thúc
            String endSessionEndpoint = UriComponentsBuilder
                    .fromUriString(issuerUri)
                    .path("/protocol/openid-connect/logout") // Endpoint logout chuẩn của OpenID Connect
                    .queryParam("id_token_hint", user.getIdToken().getTokenValue()) // Gửi ID token để Keycloak nhận diện phiên
                    .queryParam("client_id", clientId) // Thêm client_id để Keycloak biết ứng dụng nào đang yêu cầu logout
                    .toUriString();
            
            log.info("Đang đăng xuất từ Keycloak: {}", endSessionEndpoint);
            
            try {
                // Gửi request GET đến endpoint logout
                ResponseEntity<String> logoutResponse = restTemplate.getForEntity(endSessionEndpoint, String.class);
                if (logoutResponse.getStatusCode().is2xxSuccessful()) {
                    log.info("Đăng xuất từ Keycloak thành công");
                } else {
                    log.warn("Đăng xuất từ Keycloak thất bại: {}", logoutResponse.getStatusCode());
                }
            } catch (Exception e) {
                log.error("Lỗi khi đăng xuất từ Keycloak", e);
            }
        } else {
            log.info("Không có phiên người dùng hợp lệ để đăng xuất từ Keycloak");
        }
    }
    
    /**
     * Xóa token refresh từ Keycloak.
     * 
     * Phương thức này có thể được sử dụng để thu hồi refresh token, ngăn chặn
     * việc sử dụng lại token để lấy access token mới.
     * 
     * @param refreshToken Token refresh cần thu hồi
     * @return true nếu thu hồi thành công, false nếu thất bại
     */
    public boolean revokeRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            log.warn("Không thể thu hồi refresh token: token trống");
            return false;
        }
        
        try {
            // Xây dựng URL revoke token của Keycloak
            String revokeEndpoint = UriComponentsBuilder
                    .fromUriString(issuerUri)
                    .path("/protocol/openid-connect/revoke")
                    .toUriString();
            
            log.debug("Đang thu hồi refresh token tại: {}", revokeEndpoint);
            
            // TODO: Triển khai logic gửi request POST để thu hồi token
            // Đây là một phương thức mở rộng có thể được triển khai sau
            
            return true;
        } catch (Exception e) {
            log.error("Lỗi khi thu hồi refresh token", e);
            return false;
        }
    }
}
