package com.fu.flix.controller;

import com.fu.flix.dto.request.SearchServicesRequest;
import com.fu.flix.dto.request.ServiceRequest;
import com.fu.flix.dto.request.ServiceResponse;
import com.fu.flix.dto.response.SearchServicesResponse;
import com.fu.flix.service.MajorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("api/v1/major")
public class MajorController {
    private final MajorService majorService;

    public MajorController(MajorService majorService) {
        this.majorService = majorService;
    }

    @GetMapping("services")
    public ResponseEntity<ServiceResponse> getServicesByMajor(ServiceRequest request) {
        return majorService.getServicesByMajor(request);
    }

    @GetMapping("services/search")
    public ResponseEntity<SearchServicesResponse> searchService(SearchServicesRequest request) {
        return majorService.searchServices(request);
    }
}
