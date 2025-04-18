package com.example.demo.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Data Transfer Object cho User.
 * 
 * Được sử dụng để truyền thông tin người dùng giữa các lớp và trong API responses.
 * Chứa các thông tin cơ bản của người dùng từ Keycloak, bao gồm thông tin cá nhân và vai trò.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String id;          // ID của người dùng (subject) từ Keycloak
    private String username;    // Tên đăng nhập của người dùng
    private String email;       // Địa chỉ email của người dùng
    private String firstName;   // Tên của người dùng
    private String lastName;    // Họ của người dùng
    private String fullName;    // Họ và tên đầy đủ của người dùng
    
    @Builder.Default  // Đánh dấu để Lombok sử dụng giá trị mặc định khi build
    private Set<String> roles = new HashSet<>();  // Các vai trò của người dùng
    
    /**
     * Kiểm tra xem người dùng có vai trò cụ thể hay không.
     * 
     * @param role Vai trò cần kiểm tra
     * @return true nếu người dùng có vai trò, false nếu không
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
    
    /**
     * Kiểm tra xem người dùng có bất kỳ vai trò nào trong danh sách hay không.
     * 
     * @param roleList Danh sách các vai trò cần kiểm tra
     * @return true nếu người dùng có ít nhất một vai trò trong danh sách, false nếu không
     */
    public boolean hasAnyRole(String... roleList) {
        if (roles == null || roleList == null) {
            return false;
        }
        
        for (String role : roleList) {
            if (roles.contains(role)) {
                return true;
            }
        }
        
        return false;
    }
}
