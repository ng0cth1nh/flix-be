package com.fu.flix.controller;

import com.fu.flix.dto.request.GetAdminProfileRequest;
import com.fu.flix.dto.request.GetCategoriesRequest;
import com.fu.flix.dto.request.UpdateAdminProfileRequest;
import com.fu.flix.dto.response.GetAdminProfileResponse;
import com.fu.flix.dto.response.GetCategoriesResponse;
import com.fu.flix.dto.response.UpdateAdminProfileResponse;
import com.fu.flix.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("profile")
    public ResponseEntity<UpdateAdminProfileResponse> updateAdminProfile(@RequestBody UpdateAdminProfileRequest request) {
        return adminService.updateAdminProfile(request);
    }

    @GetMapping("categories")
    public ResponseEntity<GetCategoriesResponse> getCategories(GetCategoriesRequest request) {
        return adminService.getCategories(request);
    }
}
