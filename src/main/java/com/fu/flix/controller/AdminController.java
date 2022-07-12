package com.fu.flix.controller;

import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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

    @PostMapping("category")
    public ResponseEntity<CreateCategoryResponse> createCategory(CreateCategoryRequest request) throws IOException {
        return adminService.createCategory(request);
    }

    @PutMapping("category")
    public ResponseEntity<UpdateCategoryResponse> updateCategory(UpdateCategoryRequest request) throws IOException {
        return adminService.updateCategory(request);
    }
}
