package com.fu.flix.controller;

import com.fu.flix.dto.request.CancelRequestingRepairRequest;
import com.fu.flix.dto.request.DetailRequestingRepairRequest;
import com.fu.flix.dto.request.HistoryRequestingRepairRequest;
import com.fu.flix.dto.request.RequestingRepairRequest;
import com.fu.flix.dto.response.CancelRequestingRepairResponse;
import com.fu.flix.dto.response.DetailRequestingRepairResponse;
import com.fu.flix.dto.response.HistoryRequestingRepairResponse;
import com.fu.flix.dto.response.RequestingRepairResponse;
import com.fu.flix.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("api/v1/customer")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("request/repair")
    public ResponseEntity<RequestingRepairResponse> createFixingRequest(@RequestBody RequestingRepairRequest request) {
        return customerService.createFixingRequest(request);
    }

    @PostMapping("request/cancel")
    public ResponseEntity<CancelRequestingRepairResponse> cancelFixingRequest(@RequestBody CancelRequestingRepairRequest request) {
        return customerService.cancelFixingRequest(request);
    }

    @GetMapping("request/histories")
    public ResponseEntity<HistoryRequestingRepairResponse> getFixingRequestHistories(HistoryRequestingRepairRequest request) {
        return customerService.getFixingRequestHistories(request);
    }

    @GetMapping("request/detail")
    public ResponseEntity<DetailRequestingRepairResponse> getDetailFixingRequest(DetailRequestingRepairRequest request) {
        return customerService.getDetailFixingRequest(request);
    }
}
