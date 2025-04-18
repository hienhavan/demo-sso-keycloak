package com.example.demo.infrastructure.security;

import com.example.demo.presentation.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lớp tiện ích để làm việc với thông tin người dùng từ Keycloak.
 * 
 * Cung cấp các phương thức để truy cập thông tin người dùng hiện tại
 * đã xác thực qua Keycloak, bao gồm thông tin cá nhân và vai trò.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KeycloakUserUtil {

    /**
     * Lấy thông tin người dùng hiện tại từ SecurityContext.
     * 
     * @return Đối tượng UserDTO chứa thông tin người dùng, hoặc null nếu không có người dùng đăng nhập
     */
    public UserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        
        // Xử lý trường hợp người dùng đăng nhập qua form (OidcUser)
        if (principal instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) principal;
            return mapOidcUserToUserDTO(oidcUser);
        } 
        // Xử lý trường hợp xác thực qua JWT token (API)
        else if (principal instanceof Jwt) {
            Jwt jwt = (Jwt) principal;
            return mapJwtToUserDTO(jwt);
        }
        
        return null;
    }
    
    /**
     * Kiểm tra xem người dùng hiện tại có vai trò cụ thể hay không.
     * 
     * @param role Vai trò cần kiểm tra (không cần tiền tố "ROLE_")
     * @return true nếu người dùng có vai trò, false nếu không
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
    
    /**
     * Lấy danh sách vai trò của người dùng hiện tại.
     * 
     * @return Set các vai trò (không có tiền tố "ROLE_")
     */
    public Set<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Collections.emptySet();
        }
        
        return authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5)) // Loại bỏ tiền tố "ROLE_"
                .collect(Collectors.toSet());
    }
    
    /**
     * Chuyển đổi thông tin từ OidcUser sang UserDTO.
     * 
     * @param oidcUser Đối tượng OidcUser từ xác thực OAuth2
     * @return Đối tượng UserDTO chứa thông tin người dùng
     */
    private UserDTO mapOidcUserToUserDTO(OidcUser oidcUser) {
        Map<String, Object> attributes = oidcUser.getAttributes();
        
        UserDTO userDTO = new UserDTO();
        userDTO.setId(oidcUser.getSubject());
        userDTO.setUsername(oidcUser.getPreferredUsername());
        userDTO.setEmail(oidcUser.getEmail());
        userDTO.setFirstName((String) attributes.get("given_name"));
        userDTO.setLastName((String) attributes.get("family_name"));
        userDTO.setFullName(oidcUser.getFullName());
        userDTO.setRoles(getCurrentUserRoles());
        
        return userDTO;
    }
    
    /**
     * Chuyển đổi thông tin từ Jwt sang UserDTO.
     * 
     * @param jwt Đối tượng Jwt từ xác thực API
     * @return Đối tượng UserDTO chứa thông tin người dùng
     */
    private UserDTO mapJwtToUserDTO(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();
        
        UserDTO userDTO = new UserDTO();
        userDTO.setId(jwt.getSubject());
        userDTO.setUsername((String) claims.get("preferred_username"));
        userDTO.setEmail((String) claims.get("email"));
        userDTO.setFirstName((String) claims.get("given_name"));
        userDTO.setLastName((String) claims.get("family_name"));
        userDTO.setFullName((String) claims.get("name"));
        userDTO.setRoles(getCurrentUserRoles());
        
        return userDTO;
    }
}
