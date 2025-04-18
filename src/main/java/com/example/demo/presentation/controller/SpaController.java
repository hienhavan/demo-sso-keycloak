package com.example.demo.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller để phục vụ Ứng dụng Một Trang (Single Page Application).
 * Tất cả các đường dẫn không phải là đường dẫn API sẽ được chuyển tiếp đến index.html
 * để cho phép bộ định tuyến frontend xử lý chúng.
 */
@Controller
public class SpaController {

    /**
     * Chuyển tiếp tất cả các đường dẫn không phải API đến index.html
     * Điều này cho phép bộ định tuyến frontend xử lý tất cả các đường dẫn
     */
    @GetMapping(value = {"/", "/login", "/dashboard", "/admin", "/products/**"})
    public String forward() {
        return "index";
    }
}
