package com.fu.flix.controller;

import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.SearchActiveServicesResponse;
import com.fu.flix.dto.response.SearchCategoriesResponse;
import com.fu.flix.dto.response.SubServiceResponse;
import com.fu.flix.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("api/v1/category")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("services")
    public ResponseEntity<ServiceResponse> getServicesByCategory(ServiceRequest request) {
        return categoryService.getServicesByCategory(request);
    }

    @GetMapping("services/search")
    public ResponseEntity<SearchActiveServicesResponse> searchService(SearchActiveServicesRequest request) {
        return categoryService.searchServices(request);
    }

    @GetMapping("services/subServices")
    public ResponseEntity<SubServiceResponse> getSubServicesByServiceId(SubServiceRequest request) {
        return categoryService.getSubServicesByServiceId(request);
    }
}
