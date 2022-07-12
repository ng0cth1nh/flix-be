package com.fu.flix.controller;

import com.fu.flix.dto.request.GetAdminProfileRequest;
import com.fu.flix.dto.response.GetAdminProfileResponse;
import com.fu.flix.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("api/v1/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("profile")
    public ResponseEntity<GetAdminProfileResponse> getAdminProfile(GetAdminProfileRequest request) {
        return adminService.getAdminProfile(request);
    }
}
