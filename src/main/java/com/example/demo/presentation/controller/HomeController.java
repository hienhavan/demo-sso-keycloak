package com.example.demo.presentation.controller;

import com.example.demo.application.service.UserService;
import com.example.demo.domain.model.User;
import com.example.demo.presentation.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller REST cho các endpoint liên quan đến trang chủ và xác thực.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;

    /**
     * Lấy trạng thái xác thực và thông tin cơ bản của người dùng
     */
    @GetMapping("/auth-status")
    public ResponseEntity<Map<String, Object>> getAuthStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated() 
                && !authentication.getName().equals("anonymousUser");
        
        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", isAuthenticated);
        
        if (isAuthenticated) {
            userService.getCurrentUser().ifPresent(user -> {
                response.put("user", mapToDTO(user));
            });
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy dữ liệu bảng điều khiển (yêu cầu xác thực)
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        User user = userService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Người dùng không được xác thực"));
        
        Map<String, Object> dashboardData = new HashMap<>();
        dashboardData.put("user", mapToDTO(user));
        // Thêm dữ liệu bảng điều khiển nếu cần
        dashboardData.put("lastLogin", "2025-04-04T12:30:45");  // Dữ liệu ví dụ
        dashboardData.put("activityCount", 5);  // Dữ liệu ví dụ
        
        return ResponseEntity.ok(dashboardData);
    }

    /**
     * Lấy dữ liệu quản trị (yêu cầu vai trò admin)
     */
    @GetMapping("/admin-data")
    public ResponseEntity<Map<String, Object>> getAdminData() {
        User user = userService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Người dùng không được xác thực"));
        
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("user", mapToDTO(user));
        // Thêm dữ liệu dành riêng cho admin
        adminData.put("totalUsers", 42);  // Dữ liệu ví dụ
        adminData.put("totalProducts", 156);  // Dữ liệu ví dụ
        
        return ResponseEntity.ok(adminData);
    }
    
    /**
     * Chuyển đổi từ model User sang UserDTO
     */
    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .build();
    }
}
