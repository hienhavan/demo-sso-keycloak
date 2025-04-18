package com.example.demo.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Cấu hình bảo mật cho tích hợp Keycloak.
 * Lớp này cấu hình Spring Security để sử dụng Keycloak như một nhà cung cấp OAuth2/OIDC.
 * 
 * Keycloak là một giải pháp quản lý danh tính và truy cập mã nguồn mở, cung cấp các tính năng SSO,
 * xác thực và ủy quyền cho các ứng dụng web và dịch vụ.
 */
@Configuration // Đánh dấu đây là lớp cấu hình Spring
@EnableWebSecurity // Kích hoạt tính năng bảo mật web của Spring Security
@EnableMethodSecurity // Kích hoạt bảo mật ở cấp độ phương thức (cho @PreAuthorize, @Secured, etc.)
@RequiredArgsConstructor // Tự động tạo constructor với các trường final (Lombok)
public class KeycloakSecurityConfig {

    private final KeycloakLogoutHandler keycloakLogoutHandler; // Handler xử lý logout từ Keycloak
    
    @Value("${app.security.public-paths:}")
    private String[] publicPaths; // Đường dẫn công khai được cấu hình từ application.yml

    /**
     * Cấu hình SecurityFilterChain cho ứng dụng
     * 
     * SecurityFilterChain định nghĩa các filter bảo mật sẽ được áp dụng cho các request HTTP
     * 
     * @param http Đối tượng HttpSecurity để cấu hình
     * @return SecurityFilterChain đã cấu hình
     * @throws Exception nếu có lỗi trong quá trình cấu hình
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Cấu hình CSRF protection
            .csrf(csrf -> csrf
                // Sử dụng CookieCsrfTokenRepository thay vì disable hoàn toàn
                // Điều này tạo ra một cookie XSRF-TOKEN và yêu cầu header X-XSRF-TOKEN trong các request không phải GET
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                // Bỏ qua CSRF cho các endpoint API
                .ignoringRequestMatchers(new AntPathRequestMatcher("/api/**"))
            )
            // Cấu hình quyền truy cập cho các đường dẫn
            .authorizeHttpRequests(authorize -> authorize
                // Cho phép truy cập không cần xác thực vào các đường dẫn công khai
                // Bao gồm trang chủ, tài nguyên tĩnh và trang lỗi
                .requestMatchers("/", "/public/**", "/css/**", "/js/**", "/images/**", 
                        "/webjars/**", "/favicon.ico", "/error", "/actuator/health", "/actuator/info").permitAll()
                // Yêu cầu vai trò admin cho các API quản trị
                .requestMatchers("/api/admin/**").hasRole("admin")
                // Yêu cầu vai trò admin hoặc manager cho các API quản lý
                .requestMatchers("/api/manager/**").hasAnyRole("admin", "manager")
                // Tất cả các request khác đều yêu cầu xác thực
                .anyRequest().authenticated()
            )
            // Cấu hình OAuth2 Login (dùng cho form login với Keycloak)
            .oauth2Login(oauth2 -> oauth2
                // URL chuyển hướng sau khi đăng nhập thành công
                .defaultSuccessUrl("/", true)
                // URL chuyển hướng khi đăng nhập thất bại
                .failureUrl("/login?error=true")
            )
            // Cấu hình OAuth2 Resource Server (dùng cho API với JWT)
            .oauth2ResourceServer(oauth2 -> oauth2
                // Cấu hình xác thực JWT với bộ chuyển đổi tùy chỉnh để xử lý vai trò Keycloak
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            )
            // Cấu hình quản lý phiên
            .sessionManagement(session -> session
                // Tạo session khi cần thiết (phù hợp cho ứng dụng web)
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            // Cấu hình logout
            .logout(logout -> logout
                // Thêm handler xử lý logout từ Keycloak để đảm bảo đăng xuất cả từ Keycloak
                .addLogoutHandler(keycloakLogoutHandler)
                // URL chuyển hướng sau khi logout thành công
                .logoutSuccessUrl("/")
                // Xóa cookie sau khi logout
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }

    /**
     * Bộ chuyển đổi để trích xuất vai trò từ token JWT.
     * Xử lý định dạng đặc biệt của Keycloak cho vai trò trong token JWT.
     * 
     * Keycloak lưu trữ vai trò trong claim "realm_access.roles" của token JWT.
     * Phương thức này tạo một converter để chuyển đổi các vai trò này thành
     * GrantedAuthorities của Spring Security.
     * 
     * @return Converter để chuyển đổi JWT thành AbstractAuthenticationToken
     */
    private Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        // Đặt bộ chuyển đổi tùy chỉnh để xử lý vai trò Keycloak
        jwtConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        // Đặt tên thuộc tính chính để sử dụng làm username
        jwtConverter.setPrincipalClaimName("preferred_username");
        return jwtConverter;
    }

    /**
     * Bộ chuyển đổi tùy chỉnh cho vai trò realm của Keycloak.
     * Trích xuất vai trò từ token JWT và chuyển đổi chúng thành GrantedAuthorities của Spring Security.
     * 
     * Lớp này xử lý cả hai loại quyền:
     * 1. Quyền dựa trên scope từ claim "scope" (tiêu chuẩn OAuth2)
     * 2. Vai trò realm từ claim "realm_access.roles" (đặc biệt của Keycloak)
     */
    static class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        
        @Override
        @SuppressWarnings("unchecked")
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            // Tạo danh sách kết quả cuối cùng
            List<GrantedAuthority> authorities = new ArrayList<>();
            
            // 1. Thêm các quyền dựa trên scope
            // Ví dụ: scope=openid profile email sẽ tạo ra SCOPE_openid, SCOPE_profile, SCOPE_email
            JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();
            Collection<GrantedAuthority> scopeAuthorities = scopesConverter.convert(jwt);
            if (scopeAuthorities != null) {
                authorities.addAll(scopeAuthorities);
            }
            
            // 2. Thêm các vai trò realm từ token Keycloak
            // Trong token JWT của Keycloak, vai trò được lưu trong claim "realm_access.roles"
            final Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");
            
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                // Chuyển đổi mỗi vai trò thành GrantedAuthority với tiền tố "ROLE_"
                // Ví dụ: role="admin" sẽ trở thành ROLE_admin
                List<String> roles = (List<String>) realmAccess.get("roles");
                for (String role : roles) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
            }
            
            // 3. Thêm các vai trò resource từ token Keycloak (nếu có)
            // Trong một số cấu hình, Keycloak có thể đưa vai trò của resource cụ thể vào token
            Map<String, Object> resourceAccess = (Map<String, Object>) jwt.getClaims().get("resource_access");
            
            if (resourceAccess != null) {
                // Lấy client ID từ token (aud claim)
                List<String> audiences = jwt.getAudience();
                if (audiences != null && !audiences.isEmpty()) {
                    String clientId = audiences.get(0);
                    Map<String, Object> clientResource = (Map<String, Object>) resourceAccess.get(clientId);
                    
                    if (clientResource != null && clientResource.containsKey("roles")) {
                        // Chuyển đổi vai trò resource thành GrantedAuthority
                        List<String> roles = (List<String>) clientResource.get("roles");
                        for (String role : roles) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                        }
                    }
                }
            }
            
            // Trả về danh sách các quyền đã kết hợp
            return authorities;
        }
    }
    
    /**
     * Bean cung cấp JwtAuthenticationConverter để sử dụng trong các thành phần khác
     * 
     * @return JwtAuthenticationConverter đã cấu hình
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverterBean() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        converter.setPrincipalClaimName("preferred_username");
        return converter;
    }
}
