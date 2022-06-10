package com.fu.flix.controller;

import com.fu.flix.dto.request.RequestingRepairRequest;
import com.fu.flix.dto.response.RequestingRepairResponse;
import com.fu.flix.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("api/v1/customer")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("request/repair")
    public ResponseEntity<RequestingRepairResponse> getCommunesByDistrict(@RequestBody RequestingRepairRequest request) {
        return customerService.createFixingRequest(request);
    }
}
